import time
import boto3

SQS_REQUEST_QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/************/RequestQueue"
MAX_EC2_INSTANCE = 20

sqs_client = boto3.client('sqs', 'us-east-1')
ec2 = boto3.client('ec2', 'us-east-1', aws_access_key_id='********************',
                              aws_secret_access_key='****************************************')
user_data = '''#!/bin/bash
cd /home/ec2-user
aws configure set aws_access_key_id ********************
aws configure set aws_secret_access_key ****************************************
aws configure set default.region us-east-1
python3 read_q.py'''

# Gets the current request queue size and current count of running instances and decides how many new instance to create
def start_load_balance():
    print("Load balancer called")
    request_queue_length = get_sqs_queue()
    current_running_count = get_ec2_instance_running_count()
    print("Length of request queue", request_queue_length)
    print("running_count + request_queue_length", current_running_count + request_queue_length)
    if MAX_EC2_INSTANCE >= current_running_count + request_queue_length:
        for i in range(request_queue_length):
            create_apptier_ec2_instance()
            time.sleep(2)
    else:
        if request_queue_length > 0:
            for i in range(MAX_EC2_INSTANCE - current_running_count):
                create_apptier_ec2_instance()
                time.sleep(2)

# Returns the min value of three consecutive queue length fetches, this is to get a more reliable value of request queue length
def get_sqs_queue():
    newqueue = []
    for i in range(3):
        newqueue.append(get_sqs_request_queue_length())

    queue_length = min(newqueue)
    return queue_length

# This method makes a boto3 API call to get the length of request queue
def get_sqs_request_queue_length():
    sqs_queue_all_attribute = sqs_client.get_queue_attributes(
        QueueUrl=SQS_REQUEST_QUEUE_URL,
        AttributeNames=['ApproximateNumberOfMessages'])
    sqs_intime_length = sqs_queue_all_attribute['Attributes']['ApproximateNumberOfMessages']
    return int(sqs_intime_length)

# This method creates new app tier instances with specified instancetype, imageid and keyname
def create_apptier_ec2_instance():
    ec2_instance_running_count = get_ec2_instance_running_count()
    key_val = "app-instance" + str(ec2_instance_running_count)
    new_instance = ec2.run_instances(InstanceType="t2.micro",
                                                    MaxCount=1,
                                                    MinCount=1,
                                                    ImageId="ami-************",
                                                    KeyName="new_user",
                                                    SecurityGroupIds=["sg-************"],
                                                    UserData=user_data,
                                                    TagSpecifications=[{'ResourceType': 'instance',
                                                                        'Tags': [{'Key': 'Name',
                                                                                  'Value': key_val
                                                                                  }]}])
    print("Instance created successfully", new_instance)

# This method makes a boto3 API call to get the number of instances currently running
def get_ec2_instance_running_count():
    ec2_resource_det = boto3.resource('ec2', region_name='us-east-1')
    all_instances = [instance.state["Name"] for instance in ec2_resource_det.instances.all()]
    total_instance_count = all_instances.count('running') + all_instances.count('pending')
    return total_instance_count

# The load_balancer program begins here, calls another method to get request queue state and create new instances
while True:
    start_load_balance()
    time.sleep(60)
