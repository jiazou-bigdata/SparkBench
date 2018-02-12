worker_name=$1

 aws ec2 describe-instances --filters "Name=tag-value, Values=$worker_name" --query 'Reservations[].Instances[].[PrivateIpAddress]' --output=text > serverlist
