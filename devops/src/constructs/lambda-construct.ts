import {Construct} from 'constructs';
import {aws_ec2, aws_iam, aws_lambda, Duration} from 'aws-cdk-lib';

interface LambdaConstructProps {
    name: string;
    description: string;
    code_location: string;
    cloudFunctionName: string;
    vpc: aws_ec2.IVpc;
    executionRoleForLambda: aws_iam.IRole;
}

export class LambdaConstruct {

    public static create(scope: Construct, props: LambdaConstructProps) {

        return new aws_lambda.Function(scope, props.name, {
            functionName: props.name,
            description: props.description,
            role: props.executionRoleForLambda,
            vpc: props.vpc,
            memorySize: 1024,
            timeout: Duration.minutes(14),
            runtime: aws_lambda.Runtime.JAVA_17,
            handler: 'org.springframework.cloud.function.adapter.aws.FunctionInvoker',
            code: aws_lambda.Code.fromAsset(props.code_location),
            tracing: aws_lambda.Tracing.ACTIVE,
            environment: {
                "spring_cloud_function_definition": props.cloudFunctionName
            }
        });

    }
}
