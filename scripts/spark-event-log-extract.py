import sys
import json

if(len(sys.argv) < 2):
   print "Usage: #SparkEventLogJsonFileName #numCores (Optional, default=80)"
log = open( sys.argv[1], 'r' )
numCores = 80
if (len(sys.argv) >2):
   numCores = int(sys.argv[2])
print "TaskID, TaskType, Elapsed, Deserialize, DeserializeCPU, Serialize"
totalElapsed = 0.0
totalDeserialize = 0.0
totalDeserializeCPU = 0.0
totalSerialize = 0.0
for line in log:
    blob = json.loads( line )
    if blob['Event'] == 'SparkListenerTaskEnd':
        taskType = blob["Task Type"]
        elapsed_ms = int(blob['Task Info']['Finish Time']) -  int(blob['Task Info']['Launch Time'])
        deserialize_ms = int(blob['Task Metrics']['Executor Deserialize Time'])
        deserialize_cpu_ms = int(blob['Task Metrics']['Executor Deserialize CPU Time'])
        result_serialize_ms = int(blob['Task Metrics']['Result Serialization Time'])
        print "%6d, %s, %.2f, %.2f, %.2f, %.2f" % (
            blob['Task Info']['Task ID'],
            taskType,
            elapsed_ms * 1.0,
            deserialize_ms * 1.0,
            deserialize_cpu_ms * 1.0,
            result_serialize_ms * 1.0
            )
        totalElapsed += elapsed_ms
        totalDeserialize += deserialize_ms
        totalDeserializeCPU += deserialize_cpu_ms
        totalSerialize += result_serialize_ms

print "%.2f, %.2f, %.2f, %.2f" % (
             totalElapsed / 1000.0,
             totalDeserialize / 1000.0,
             totalDeserializeCPU / 1000.0,
             totalSerialize / 1000.0
             )

print "%.2f, %.2f, %.2f, %.2f" % (
             totalElapsed / 1000.0 / 80.0,
             totalDeserialize / 1000.0 / 80.0,
             totalDeserializeCPU / 1000.0 / 80.0,
             totalSerialize / 1000.0 / 80.0
             )
