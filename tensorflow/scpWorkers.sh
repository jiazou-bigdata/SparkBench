#!/bin/bash
# by Jia


user=$1
key=$2
serverlist=$3
sources=$4
targets=$5

arr=($(awk '{print $0}' $serverlist))
length=${#arr[@]}
echo "There are $length servers"
for (( i=0 ; i<$length ; i++ ))
do
        host=${arr[i]}
        echo -e "\n+++++++++++ reconfig: $host"
        scp -r -i $key $sources $user@$host:$targets
done
