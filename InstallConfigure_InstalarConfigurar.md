#Como instalar e configurar a aplicação. How to install and configure the application.

## Comming Soon / Em Breve ##

# rascunho #

  * Instalar o java (versão 6 ou superior ).

  * Baixar o tomcat6 ( versão zip ) e extrair em ( por exemplo ) : c\tomcat6\.

  * Copiar o arquivo que veio no download do openodonto 'mysql-connector-java-5.1.7-bin.jar' para : c:\tomcat6\lib

  * Copiar o arquivo openodonto.war dentro de 'c:\tomcat6\webapps'

  * Intalar o mysql

  * Conectar-se ao mysql:
> `mysql -u root -p`
    * Criar uma base de dados :
> `create database openodonto`
    * Sair do mysql :
> `exit`
    * Executar a DDL :
> `mysql -u root -p openodonto < DDL_Iteracao_5.sql`
> Obs.:È preciso executar esse comando dentro do path onde o arquivo .sql se encontra)

  * Entrar no mysql e criar uma pessoa e um usuário :
    * `mysql -u root -p openodonto`
    * `insert into pessoas(nome) values ("Administrador")`,
    * `insert into usuarios(id_pessoa,user,senha) values ("1","admin","21232f297a57a5a743894a0e4a801fc3")`

  * Criar o arquivo 'C:\tomcat6\conf\Catalina\localhost\openodonto.xml' com o conteúdo:

```xml

<Context path="/openodonto" docBase="openodonto" debug="0" reloadable="true" crossContext="true">

<Loader delegate="true"/>

<Resource name="jdbc/openodonto-cpds"
auth="Container"
type="org.apache.tomcat.dbcp.dbcp.cpdsadapter.DriverAdapterCPDS"
factory="org.apache.tomcat.dbcp.dbcp.cpdsadapter.DriverAdapterCPDS"
driver="com.mysql.jdbc.Driver"
url="jdbc:mysql://localhost:3306/openodonto?autoReconnect=true"
user="root"
password="12345"/>

<Resource name="jdbc/openodonto"
auth="Container"
type="org.apache.tomcat.dbcp.dbcp.datasources.SharedPoolDataSource"
factory="org.apache.tomcat.dbcp.dbcp.datasources.SharedPoolDataSourceFactory"
dataSourceName="java:comp/env/jdbc/openodonto-cpds"
maxActive="30"
maxIdle="5"
maxWait="10000"
removeAbandonedTimeout="40"
logAbandoned="true"
removeAbandoned="true"
validationQuery="SELECT 1"
testOnBorrow="true"
testOnReturn="true"
testWhileIdle="true"
timeBetweenEvictionRunsMillis="60000"
numTestsPerEvictionRun="15"
minEvictableIdleTimeMillis="60000"/>



Unknown end tag for &lt;/Context&gt;


```

  * Executar o comando 'c:\tomcat\bin\startup.bat'
  * Acessar o firefox em : http://localhost:8080/openodonto
  * Autenticar com admin, admin