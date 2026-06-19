#!/bin/bash

TOMCAT_PATH="/home/tomefy/Documents/tomcat/tomcat"

echo "Construction du projet..."
mvn clean package
mvn clean install

JAR_FILE=$(find target -name "*.jar" \
    ! -name "*sources*" \
    ! -name "*javadoc*" \
    ! -name "original-*" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "Aucun JAR trouvé."
    exit 1
fi

echo "Copie du JAR vers Tomcat..."
cp "$JAR_FILE" "$TOMCAT_PATH/lib/"

echo "Déploiement terminé : $JAR_FILE"