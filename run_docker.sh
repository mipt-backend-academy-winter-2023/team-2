exec 5>&1
dockerResult=$(docker build . | tee >(cat - >&5))
buildId=$(echo "$dockerResult" | tail -n 1)

buildId="${buildId#*Successfully built }"
echo $buildId
docker run -p 8082:8082 $buildId
