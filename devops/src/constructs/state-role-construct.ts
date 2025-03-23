import {Construct} from 'constructs';
import {aws_iam} from 'aws-cdk-lib';
import * as iam from 'aws-cdk-lib/aws-iam';

export class StateRoleConstruct {

    public static create(scope: Construct, roleName: string) {

        return new aws_iam.Role(scope, "StateRole", {
            roleName: roleName,
            assumedBy: new aws_iam.ServicePrincipal('states.amazonaws.com'),
            inlinePolicies: {
                cloudMetricDataPolicy: this.cloudMetricDataPolicy(),
                lambdaPolicy: this.lambdaPolicy()
            },
            managedPolicies: [
                aws_iam.ManagedPolicy.fromAwsManagedPolicyName(
                    'service-role/AWSLambdaVPCAccessExecutionRole'
                )
            ],
            permissionsBoundary: aws_iam.ManagedPolicy.fromManagedPolicyName(
                scope,
                'RoleBoundaryPolicy',
                'boundary-policy'
            )
        });

    }

    private static cloudMetricDataPolicy() {
        return new iam.PolicyDocument({
            statements: [
                new iam.PolicyStatement({
                    actions: [
                        'cloudwatch:PutMetricData'
                    ],
                    effect: iam.Effect.ALLOW,
                    resources: ['*']
                })
            ]
        });
    }

    private static lambdaPolicy() {
        return new iam.PolicyDocument({
            statements: [
                new iam.PolicyStatement({
                    actions: ['lambda:InvokeFunction'],
                    effect: iam.Effect.ALLOW,
                    resources: ['*']
                })
            ]
        });
    }

}
