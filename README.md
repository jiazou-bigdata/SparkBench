ganglia-extract.py

location: https://github.com/jiazou-bigdata/SparkBench/blob/master/scripts/ganglia-extract.py

usage:
python scripts/ganglia-extract.py #ganglia-rrds-directory #interval-to-collect-stats #duration-ahead-of-current-time

purpose:
to poll rrd data from the rrd buffer starting from (current-time - duration-ahead-of-current-time) until you stop the script by CTRL-C
all polled rrd data is written to csv files in ganglia-output in current directory

example:
python scripts/ganglia-extract.py /var/lib/ganglia/rrds/TestingCluster 15
python scripts/ganglia-extract.py /var/lib/ganglia/rrds/TestingCluster 15 60 
