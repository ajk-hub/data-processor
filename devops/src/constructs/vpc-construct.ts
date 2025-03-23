import {Construct} from 'constructs';
import {aws_ec2, CfnParameter} from 'aws-cdk-lib';
import {SsmParameterStore} from '../utils/ssm-parameter-store';

export class VpcConstruct extends Construct {
    public readonly vpc: aws_ec2.IVpc;

    constructor(scope: Construct, id: string) {
        super(scope, 'Vpc');

        this.vpc = aws_ec2.Vpc.fromVpcAttributes(this, id, {
            vpcId: this.createParamVPCId(),
            vpcCidrBlock: this.createParamVpcSubnetCidr(),
            availabilityZones: [this.createParamAvailZone()],
            privateSubnetIds: [this.createParamVpcSubnetId()],
            privateSubnetRouteTableIds: [this.createParamVpcSubnetRouteTable()]
        });
    }

    private createParamVPCId() {
        return SsmParameterStore.getValue({
            scope: this,
            id: 'Id',
            parameterName: '/vpc/vpcid',
            description: 'Private VPC id for things like lambda ENIs'
        });
    }

    private createParamAvailZone() {
        return new CfnParameter(this, 'AvailZone', {
            default: 'us-east-1a',
            type: 'String',
            description: 'Private VPC subnet avail zone for things like lambda ENIs'
        }).valueAsString;
    }

    private createParamVpcSubnetId() {
        return SsmParameterStore.getValue({
            scope: this,
            id: 'SubnetId',
            parameterName: '/vpc/privatesubnetid',
            description: 'Private VPC subnet id for things like lambda ENIs'
        });
    }

    private createParamVpcSubnetCidr() {
        return SsmParameterStore.getValue({
            scope: this,
            id: 'SubnetCidr',
            parameterName: '/vpc/privatesubnetcidr',
            description: 'CIDR for Subnet'
        });
    }

    private createParamVpcSubnetRouteTable() {
        return SsmParameterStore.getValue({
            scope: this,
            id: 'RouteTable',
            parameterName: '/vpc/privatesubnetroutetable',
            description: 'Private VPC route table'
        });
    }

}
