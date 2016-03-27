#!/usr/bin/env bash
##########################################################
# Executa curl simultaneamente contra URL multiplas vezes
# Params: $1 -> número de requisicoes
# Params: $2 -> URL
#
# curl 100 localhost/api/test
# Esse comando execute 100 requições no na URL 'localhost/api/test/$1' ONDE $1 é o número da requisão

for i in `seq 1 $1`;
do
    curl $2/${i} &
done