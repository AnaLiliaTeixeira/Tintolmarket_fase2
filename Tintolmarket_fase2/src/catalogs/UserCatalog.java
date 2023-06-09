package catalogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import application.TintolmarketServer;
import entities.User;
import exceptions.WrongCredentialsException;
import utils.Utils;

/**
 * A classe UserCatalog e responsavel por gerir o catalogo de utilizadores. Esta
 * classe permite criar um novo utilizador, adicionar um utilizador e fazer
 * login de um utilizador.
 */
public class UserCatalog {

	private static UserCatalog instance;
	private File userInfo;
	private List<User> users;
	private static KeyStore keyStore;
	private static SecretKey fileKey;

	/**
	 * Construtor privado da classe UserCatalog.
	 */
	private UserCatalog() {
		keyStore = TintolmarketServer.getKeyStore();
		fileKey = TintolmarketServer.getFileKey();
		users = new ArrayList<>();
		File txtFolder = new File("txtFiles");
		userInfo = new File("txtFiles//userCatalog.txt");
		try {
			if (!txtFolder.exists())
				txtFolder.mkdir();
			if (!userInfo.exists()) {
				userInfo.createNewFile();
				Utils.updateHash(userInfo);
			} else
				getUsersByTextFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retorna uma instancia do catalogo de utilizadores.
	 * 
	 * @return uma instancia do catalogo de utilizadores.
	 */
	public static UserCatalog getInstance() {
		if (instance == null) {
			instance = new UserCatalog();
		}
		return instance;
	}

	/**
	 * Efetua o login do utilizador ou cria um novo utilizador.
	 * 
	 * @param in       ObjectInputStream para ler dados do servidor.
	 * @param out      ObjectOutputStream para enviar dados para o servidor.
	 * @param keyStore a keystore do server
	 * @return o nome de utilizador se o login for bem-sucedido, ou null caso
	 *         contrario.
	 * @throws Exception Se ocorrer um erro
	 */
	public synchronized String login(ObjectInputStream in, ObjectOutputStream out) throws Exception {
		File users = new File("txtFiles//userCreds.txt");
		if (!users.exists()) {
			users.createNewFile();
			Utils.updateHash(users);
		}
		Scanner sc = new Scanner(users);

		// le user e verifica se ja existe
		String user = in.readUTF();

		boolean newUser = true;
		String certName = "";
		String line;
		while (sc.hasNextLine()) {
			line = Utils.cipherSymmetricString(Cipher.DECRYPT_MODE, fileKey, sc.nextLine());
			if (line.startsWith(user)) {
				newUser = false;
				certName = line.split(":")[1];
				break;
			}
		}
		sc.close();

		SecureRandom rd = new SecureRandom();
		byte[] nonce = new byte[8];
		rd.nextBytes(nonce);
		out.writeObject(nonce);
		out.flush();

		boolean result = true;
		out.writeBoolean(newUser);
		out.flush();
		if (newUser) { // se o user nao existir faz o seu registo
			result = registerUser(in, out, keyStore, user, nonce);
		} else { // se user existir
			byte[] encryptedNonce = (byte[]) in.readObject();
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			FileInputStream fis = new FileInputStream("stores//server//" + certName);
			Certificate certificate = certificateFactory.generateCertificate(fis);
			fis.close();
			result = Utils.verifySignature(certificate.getPublicKey(), nonce, encryptedNonce);
		}

		if (!result)
			throw new WrongCredentialsException("Credenciais invalidas");

		return user;
	}

	/**
	 * Regista um novo utilizador no catalogo
	 * 
	 * @param in       stream de input
	 * @param out      stream de output
	 * @param keyStore a keystore a usar
	 * @param user     o nome do novo utilizador
	 * @param nonce    o nonce de verificacao gerado
	 * @return true se o registo foi efetuado com sucesso, false caso contrario
	 * @throws Exception
	 */
	private boolean registerUser(ObjectInputStream in, ObjectOutputStream out, KeyStore keyStore, String user,
			byte[] nonce) throws Exception {
		boolean result = true;

		byte[] recievedNonce = (byte[]) in.readObject();
		result = Arrays.equals(recievedNonce, nonce);
		if (!result)
			return false;

		byte[] encryptedNonce = (byte[]) in.readObject(); // receber assinatura e certificado
		Certificate cert = (Certificate) in.readObject();

		// verificar assinatura
		result = Utils.verifySignature(cert.getPublicKey(), recievedNonce, encryptedNonce);

		// se bem sucedido guardar certificado e user
		if (result) {
			File certFile = new File("stores//server//keyRSApub_" + user + ".cer");
			certFile.createNewFile();
			FileWriter fw = new FileWriter(certFile);
			Base64.Encoder encoder = Base64.getMimeEncoder(64, System.getProperty("line.separator").getBytes());
			fw.write("-----BEGIN CERTIFICATE-----\n");
			fw.write(encoder.encodeToString(cert.getEncoded()));
			fw.write("-----END CERTIFICATE-----\n");
			fw.close();

			this.addUser(user);
			File creds = new File("txtFiles//userCreds.txt");
			fw = new FileWriter(creds, true);
			fw.write(Utils.cipherSymmetricString(Cipher.ENCRYPT_MODE, fileKey, user + ":" + certFile.getName())
					+ "\r\n");
			fw.close();
			Utils.updateHash(creds);
		}
		return result;
	}

	/**
	 * Le e armazena os utilizadores do ficheiro de texto userInfo.
	 * 
	 * @param userInfo O arquivo de texto com as informacoes dos utilizadores.
	 */
	private void getUsersByTextFile() {
		try {
			Scanner sc = new Scanner(userInfo);
			while (sc.hasNextLine()) {
				String[] line = Utils.cipherSymmetricString(Cipher.DECRYPT_MODE, fileKey, sc.nextLine())
						.split("(?!\\{.*)\\s(?![^{]*?\\})");
				users.add(new User(line[0], Double.parseDouble(line[1]), stringToHashMap(line[2])));
			}
			sc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retorna um objeto User com base no nome do utilizador.
	 * 
	 * @param userName O nome do utilizador.
	 * @return Um objeto User correspondente ao nome do utilizador.
	 */
	public User getUserByName(String userName) {
		for (User u : this.users)
			if (u.getName().equals(userName))
				return u;
		return null;
	}

	/**
	 * Adiciona um novo utilizador a lista de utilizadores e ao arquivo de texto.
	 * 
	 * @param userName O nome do novo utilizador.
	 */
	public synchronized void addUser(String userName) {
		try {
			User u = new User(userName, 200, new HashMap<>());
			this.users.add(u);
			File userInfo = new File("txtFiles//userCatalog.txt");
			FileWriter fw = new FileWriter(userInfo, true);
			fw.write(Utils.cipherSymmetricString(Cipher.ENCRYPT_MODE, fileKey, u.toString()) + "\r\n");
			fw.close();
			Utils.updateHash(userInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Obtem a chave publica de um user
	 * 
	 * @param userId o nome do user
	 * @return a chave publica
	 */
	public PublicKey getPublicKey(String userId) {
		try {
			return getUserByName(userId).getCertificate().getPublicKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Converte uma string em um HashMap de strings e listas de strings.
	 * 
	 * @param line A string a ser convertida.
	 * @return Um HashMap com strings como chaves e listas de strings como valores.
	 */
	public HashMap<String, List<String>> stringToHashMap(String line) {
		HashMap<String, List<String>> result = new HashMap<>();
		line = line.substring(1, line.length() - 1);
		String[] hashContents = line.split("(?!\\[.*), (?![^\\[]*?\\])");
		if (hashContents[0].contains("=")) {
			for (String s : hashContents) {
				String[] item = s.split("=", 2);
				item[1] = item[1].substring(1, item[1].length() - 1);
				List<String> value = Arrays.asList(item[1].split(", "));
				result.put(item[0], value);
			}
		}
		return result;
	}

}
