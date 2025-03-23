export class LambdaCodeLocation {

    private static JAR_FILE = 'data-processor-1.0.0-SNAPSHOT-aws.jar';

    public static get(environment: string): string {
        return environment === 'prod'
            ? `../${LambdaCodeLocation.JAR_FILE}`
            : `../target/${LambdaCodeLocation.JAR_FILE}`;
    }

}
