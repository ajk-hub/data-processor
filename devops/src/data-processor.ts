#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import {DataProcessorStack} from './stack/data-processor-stack';

const defaultStackSynthesizerProps: cdk.DefaultStackSynthesizerProps = {
    bucketPrefix: '',
    cloudFormationExecutionRole: 'arn:${AWS::Partition}:iam::${AWS::AccountId}:role/delegate-admin-${AWS::AccountId}'
};

const app = new cdk.App();

new DataProcessorStack(app, 'DataProcessorStack', {
    stackName: `data-processor-stack`,
    description: 'Data Processor Stack',
    synthesizer: new cdk.DefaultStackSynthesizer(defaultStackSynthesizerProps)
});

cdk.Tags.of(app).add("environment", "Development");
