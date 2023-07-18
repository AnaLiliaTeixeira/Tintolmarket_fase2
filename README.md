# Tintolmarket Fase 2 - Compilar e Executar

Este documento explica como compilar e executar o projeto Tintolmarket Fase 2. Na <a href="https://github.com/AnaLiliaTeixeira/Tintolmarket_fase1">primeira fase</a> do projeto foram implementadas as funcionalidades básicas do serviço, como a interação entre a aplicação Cliente e Servidor, e a gestão das informações dos utilizadores e vinhos.

Agora, na segunda fase do projeto, o foco será nos requisitos de segurança, garantindo que as interações e o sistema como um todo sejam seguros. As funcionalidades da primeira fase serão mantidas, mas em alguns casos, a implementação será adaptada para cumprir os requisitos de segurança.

O foco será na segurança da arquitetura do sistema. As principais alterações incluem:

1. Comunicações através de sockets seguros TLS com autenticação unilateral.

2. Armazenamento das chaves privadas em keystores protegidas por senhas.

3. Uso de certificados de chave pública auto-assinados em uma truststore compartilhada.

4. Geração de pares de chaves RSA de 2048 bits.

5. Cifragem do ficheiro de utilizadores no servidor usando PBE com AES de 128 bits.

6. Verificação de integridade dos ficheiros mantidos pelo servidor.

7. Criação de um log seguro em forma de blockchain para registrar transações.

8. Confidencialidade end-to-end nas mensagens trocadas entre clientes.

9. Essas medidas de segurança visam garantir a proteção das informações, a integridade dos dados e a privacidade das comunicações entre os utilizadores do sistema Tintolmarket.

***

# Requisitos

Antes de começar, certifique-se de que possui as seguintes ferramentas instaladas:

+ Java Development Kit (JDK) versão 8 ou superior

***

# Compilar

1. Abra um terminal na pasta root do projeto  
2. Compile os arquivos Java do Servidor e do Cliente utilizando o seguinte comando:  
````
javac TintolmarketServer.java TintolmarketClient.java
````

***

# Executar

Após compilar o projeto, siga os passos abaixo para executar os programas.

## Executar o Servidor TintoImarketServer  

1. Abra um terminal e navegue até a pasta onde se encontram os arquivos Java compilados.  
2. Execute o seguinte comando para iniciar o servidor, substituindo ````<port>````, ````<password-cifra>````, ````<keystore>```` e ````<password-keystore>```` pelos valores correspondentes (p.e., 12345 para a porta):

````
java TintolmarketServer <port> <password-cifra> <keystore> <password-keystore>
````

## Executar a aplicação Cliente TintoImarket  

1. Abra um novo terminal e navegue até a pasta onde se encontram os arquivos Java compilados.  
2. Execute o seguinte comando para iniciar a aplicação cliente, substituindo ````<serverAddress>````, ````<truststore>````, ````<keystore>````, ````<password-keystore>```` e ````<userID>```` pelos valores correspondentes: 

````
java TintolmarketClient <serverAddress> <truststore> <keystore> <password-keystore> <userID>
````  
Agora pode começar a utilizar o sistema Tintolmarket para adicionar vinhos, indicar quantidades disponíveis, classificar vinhos e enviar mensagens privadas a outros utilizadores. As passwords das stores default são 123456.

***
# Adicionar Segurança ao Sistema

1. Canais seguros **TLS** para comunicação segura e autenticação de
servidores

2. Autenticação de utilizadores

3. Criação de um *log* seguro para transações

4. Confidencialidade *fim-a-fim* para as mensagens

***
# Operações Disponíveis

A aplicação cliente oferece várias operações que pode executar. Alguns exemplos incluem:  

+ `add <wine> <image>`: Adicionar um novo vinho à lista.
+ `sell <wine> <value> <quantity>`: Colocar um vinho à venda com um preço e quantidade especificados.
+ `view <wine>`: Visualizar informações de um vinho específico, como imagem, classificação média e disponibilidade.
+ `buy <wine> <seller> <quantity>`: Comprar uma quantidade específica de um vinho de outro utilizador.
+ `wallet` : Devolver o saldo atual da carteira
+ `classify <wine> <stars>`: Atribuir uma classificação de 1 a 5 a um vinho específico.
+ `talk <user> <message>`: Enviar uma mensagem privada a outro utilizador.
+ `read`: Ler as novas mensagens recebidas e apresentar a identificação do remetente e a respectiva mensagem.
+ `exit`: Terminar o programa cliente.


Adicionada nova funcionalidade na segunda fase:
+ `list`: Obtém a lista de todas as transações que já foram efetuadas e que se encontram
armazenadas na blockchain. 

***
# Limitações

+ Assume que o Servidor tem permissão para escrever no diretório em que está a ser executado.  
  
+ Certificados auto-assinados: O uso de certificados auto-assinados pode tornar o sistema vulnerável a ataques *man-in-the-middle*, já que não há uma autoridade certificadora (CA) que verifica a autenticidade do certificado.
***

# Autores

Ana Teixeira 56336  
Francisco Abreu 56277  
João Matos 56292

***

# Data

19/03/2023
