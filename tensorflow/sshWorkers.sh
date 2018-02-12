#!/bin/bash
# by Jia


user=$1
key=$2
serverlist=$3
commands=$4


arr=($(awk '{print $0}' $serverlist))
length=${#arr[@]}
echo "There are $length servers"
for (( i=0 ; i<$length ; i++ ))
do
        host=${arr[i]}
        echo -e "\n+++++++++++ reconfig: $host"
        ssh -i $key $user@$host $commands
done
