#!/bin/bash

TOMCAT_PATH="/home/tomefydirk/Documents/deploy/tomcat"

echo "=========================================="
echo "  BUILD : framework"
echo "=========================================="

echo "--- Étape 1 : Compilation + installation ---"
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ La compilation du framework a échoué."
    exit 1
fi

echo "✅ Compilation réussie."

echo "--- Étape 2 : Localisation du JAR ---"
JAR_FILE=$(find target -maxdepth 1 -name "*.jar" \
    ! -name "*sources*" \
    ! -name "*javadoc*" \
    ! -name "original-*" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ Aucun JAR trouvé dans target/"
    exit 1
fi

echo "JAR trouvé : $JAR_FILE"

echo "--- Étape 3 : Vérification du contenu ---"
if ! jar tf "$JAR_FILE" | grep -q "com/dirkfw/session/DfwSession.class"; then
    echo "❌ DfwSession.class absent du JAR."
    echo "   Vérifie que le fichier est bien dans :"
    echo "   src/main/java/com/dirkfw/session/DfwSession.java"
    exit 1
fi


echo "--- Étape 4 : Déploiement vers Tomcat ---"
cp "$JAR_FILE" "$TOMCAT_PATH/lib/"

if [ $? -ne 0 ]; then
    echo "❌ Copie vers Tomcat échouée."
    exit 1
fi

echo " Déploiement terminé : $JAR_FILE"