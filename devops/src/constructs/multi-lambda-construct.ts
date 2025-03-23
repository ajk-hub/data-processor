import {Construct} from 'constructs';
import {LambdaConstruct} from './lambda-construct';
import {aws_ec2, aws_iam, aws_lambda} from 'aws-cdk-lib';

interface MultiLambdaConstructProps {
    lambdaCodeLocation: string;
    vpc: aws_ec2.IVpc;
    executionRoleForLambda: aws_iam.IRole;
}

export class MultiLambdaConstruct {

    readonly initLambda: aws_lambda.Function;
    readonly syncUpLambda: aws_lambda.Function;
    readonly preStagingLambda: aws_lambda.Function;
    readonly stagingLambda: aws_lambda.Function;
    readonly prePublishLambda: aws_lambda.Function;
    readonly publishLambda: aws_lambda.Function;

    constructor(scope: Construct, props: MultiLambdaConstructProps) {

        this.initLambda = LambdaConstruct.create(scope, {
            name: 'init-lambda',
            description: 'Init Lambda',
            code_location: props.lambdaCodeLocation,
            cloudFunctionName: 'initLambda',
            vpc: props.vpc,
            executionRoleForLambda: props.executionRoleForLambda
        });

        this.syncUpLambda = LambdaConstruct.create(scope, {
            name: 'sync-up-lambda',
            description: 'Sync Up Lambda',
            code_location: props.lambdaCodeLocation,
            cloudFunctionName: 'syncUpLambda',
            vpc: props.vpc,
            executionRoleForLambda: props.executionRoleForLambda
        });

        this.preStagingLambda = LambdaConstruct.create(scope, {
            name: 'pre-staging-lambda',
            description: 'Pre-Staging Lambda',
            code_location: props.lambdaCodeLocation,
            cloudFunctionName: 'preStagingLambda',
            vpc: props.vpc,
            executionRoleForLambda: props.executionRoleForLambda
        });

        this.stagingLambda = LambdaConstruct.create(scope, {
            name: 'staging-lambda',
            description: 'Staging Lambda',
            code_location: props.lambdaCodeLocation,
            cloudFunctionName: 'stagingLambda',
            vpc: props.vpc,
            executionRoleForLambda: props.executionRoleForLambda
        });

        this.prePublishLambda = LambdaConstruct.create(scope, {
            name: 'pre-publish-lambda',
            description: 'Pre-Publish Lambda',
            code_location: props.lambdaCodeLocation,
            cloudFunctionName: 'prePublishLambda',
            vpc: props.vpc,
            executionRoleForLambda: props.executionRoleForLambda
        });

        this.publishLambda = LambdaConstruct.create(scope, {
            name: 'publish-lambda',
            description: 'Publish Lambda',
            code_location: props.lambdaCodeLocation,
            cloudFunctionName: 'publishLambda',
            vpc: props.vpc,
            executionRoleForLambda: props.executionRoleForLambda
        });

    }

}
