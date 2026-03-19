@echo off
setlocal

REM ====== CONFIGURATION ======
set APP_NAME=reservation
set TOMCAT_WEBAPPS=C:\xampp\tomcat_2\webapps
set SRC_DIR=src
set WEBAPP_DIR=webapp
set LIB_DIR=lib
set BUILD_DIR=build

REM ====== Java 21 (requis pour myframework.jar) ======
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

REM ====== Dépendances ======
REM Téléchargez les JARs suivants et placez-les dans le dossier lib/ :
REM   - postgresql-42.x.x.jar   (JDBC PostgreSQL)
REM   - gson-2.x.jar            (JSON)
REM   - jakarta.servlet-api-5.0.0.jar (API Servlet pour compilation)
REM   - myframework.jar         (votre framework)

echo ==== Nettoyage ====
rmdir /S /Q "%BUILD_DIR%" 2>nul
mkdir "%BUILD_DIR%\WEB-INF\classes"
mkdir "%BUILD_DIR%\WEB-INF\lib"

echo ==== Compilation ====
set CLASSPATH=%LIB_DIR%\*

dir /s /b %SRC_DIR%\*.java > sources.txt
javac -source 21 -target 21 -d "%BUILD_DIR%\WEB-INF\classes" -cp "%CLASSPATH%" @sources.txt
if errorlevel 1 (
    echo Erreur de compilation!
    del sources.txt
    exit /b 1
)
del sources.txt

echo ==== Copie des fichiers ====
xcopy /E /Y "%WEBAPP_DIR%\*" "%BUILD_DIR%\" >nul
copy /Y "%LIB_DIR%\*.jar" "%BUILD_DIR%\WEB-INF\lib\" >nul

echo ==== Création du WAR ====
cd "%BUILD_DIR%"
jar -cvf "%APP_NAME%.war" .
cd ..

echo ==== Déploiement vers Tomcat ====
if exist "%TOMCAT_WEBAPPS%" (
    copy /Y "%BUILD_DIR%\%APP_NAME%.war" "%TOMCAT_WEBAPPS%\" >nul
    echo Déployé dans %TOMCAT_WEBAPPS%\%APP_NAME%.war
) else (
    echo ATTENTION: Le dossier Tomcat n'existe pas: %TOMCAT_WEBAPPS%
)

echo.
echo ==== Generation du Token API ====
java -cp "%BUILD_DIR%\WEB-INF\classes" security.TokenService
echo.

echo ==== Terminé ====
echo Pour accéder à l'application:
echo   - Formulaire: http://localhost:8383/%APP_NAME%/reservations/new
echo   - API REST:   http://localhost:8383/%APP_NAME%/api/reservations
echo.
endlocal
