import {Construct} from 'constructs';
import * as cdk from 'aws-cdk-lib';
import {aws_iam} from 'aws-cdk-lib';
import {LambdaCodeLocation} from '../utils/lambda-code-location';
import {VpcConstruct} from '../constructs/vpc-construct';
import {DataProcessorConstruct} from './data-processor-construct';

export class DataProcessorStack extends cdk.Stack {

    constructor(scope: Construct, id: string, props: cdk.StackProps) {
        super(scope, id, props);

        const environment = this.node.tryGetContext("environment");
        const lambdaCodeLocation = LambdaCodeLocation.get(environment);

        console.log(`Stack Environment : [${environment}]`);
        console.log(`JAR Location : [${lambdaCodeLocation}]`);

        const vpc = new VpcConstruct(this, 'Vpc').vpc;
        const executionRoleForLambda = aws_iam.Role.fromRoleName(this, 'LambdaRole', 'delegate-admin-lambda-role');

        new DataProcessorConstruct(this, {lambdaCodeLocation, vpc, executionRoleForLambda});

    }
}
