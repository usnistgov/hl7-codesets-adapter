
echo "********************************************************"
echo "Starting Codesets adapter  "
echo "********************************************************"



echo "Host DB is : ${MONGODB_URI}"


java -Xmx900m \
     -Xss256k \
     -Djava.security.egd=file:/dev/./urandom  \
     -jar /usr/local/codesets-adapter/codesets-adapter.jar








