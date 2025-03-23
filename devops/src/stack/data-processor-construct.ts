import {Construct} from 'constructs';
import {aws_ec2, aws_iam, aws_stepfunctions, aws_stepfunctions_tasks, Duration} from 'aws-cdk-lib';
import {MultiLambdaConstruct} from '../constructs/multi-lambda-construct';
import {StateRoleConstruct} from '../constructs/state-role-construct';

interface DataProcessorConstructProps {
    lambdaCodeLocation: string;
    vpc: aws_ec2.IVpc;
    executionRoleForLambda: aws_iam.IRole;
}

export class DataProcessorConstruct {

    constructor(scope: Construct, props: DataProcessorConstructProps) {

        const multiLambda = new MultiLambdaConstruct(scope, {...props});
        const initLambda = multiLambda.initLambda;
        const syncUpLambda = multiLambda.syncUpLambda;
        const preStagingLambda = multiLambda.preStagingLambda;
        const stagingLambda = multiLambda.stagingLambda;
        const prePublishLambda = multiLambda.prePublishLambda;
        const publishLambda = multiLambda.publishLambda;

        const initTask = new aws_stepfunctions_tasks.LambdaInvoke(scope, "Init-Task", {
            lambdaFunction: initLambda,
            outputPath: "$.Payload"
        });

        const syncUpTask = new aws_stepfunctions_tasks.LambdaInvoke(scope, "SyncUp-Task", {
            lambdaFunction: syncUpLambda,
            outputPath: "$.Payload"
        });

        const preStagingTask = new aws_stepfunctions_tasks.LambdaInvoke(scope, "PreStaging-Task", {
            lambdaFunction: preStagingLambda,
            outputPath: "$.Payload"
        });

        const stagingTask = new aws_stepfunctions_tasks.LambdaInvoke(scope, "Staging-Task", {
            lambdaFunction: stagingLambda,
            outputPath: "$.Payload"
        });

        const stagingMap = new aws_stepfunctions.Map(scope, "Staging-Parallel", {
            maxConcurrency: 5,
            itemsPath: "$.stagingItems"
        }).itemProcessor(stagingTask);

        const prePublishTask = new aws_stepfunctions_tasks.LambdaInvoke(scope, "PrePublish-Task", {
            lambdaFunction: prePublishLambda,
            outputPath: "$.Payload"
        });

        const publishTask = new aws_stepfunctions_tasks.LambdaInvoke(scope, "Publish-Task", {
            lambdaFunction: publishLambda,
            outputPath: "$.Payload"
        });

        const publishMap = new aws_stepfunctions.Map(scope, "Publish-Parallel", {
            maxConcurrency: 5,
            itemsPath: "$.publishItems"
        }).itemProcessor(publishTask);

        const definition = initTask
            .next(syncUpTask)
            .next(preStagingTask)
            .next(stagingMap)
            .next(prePublishTask)
            .next(publishMap);

        new aws_stepfunctions.StateMachine(scope, "ProcessStateMachine", {
            definitionBody: aws_stepfunctions.DefinitionBody.fromChainable(definition),
            timeout: Duration.minutes(15),
            role: StateRoleConstruct.create(scope, "delegate-admin-step-role")
        });
    }

}
