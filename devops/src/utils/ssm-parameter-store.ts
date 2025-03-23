import {CfnParameter} from 'aws-cdk-lib';
import {Construct} from 'constructs';

export class SsmParameterStore {

    public static getValue(props: {
        scope: Construct;
        id: string;
        parameterName: string;
        description: string;
    }): string {
        return new CfnParameter(props.scope, props.id, {
            default: props.parameterName,
            type: 'AWS::SSM::Parameter::Value<String>',
            description: props.description
        }).valueAsString;
    }

}
