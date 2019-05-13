#import "OnFidoBridge.h"

#import <Cordova/CDVAvailability.h>
#import <Onfido/Onfido-Swift.h>

@implementation OnFidoBridge

- (void)pluginInitialize {
}

- (void)init: (CDVInvokedUrlCommand *)command {
    NSDictionary* options = [command.arguments objectAtIndex:0];
    NSString* applicantId = [options objectForKey:@"applicant_id"];
    NSString* token = [options objectForKey:@"token"];
    NSArray* flowSteps = [options objectForKey:@"flow_steps"];
    NSArray* documentTypes = [options objectForKey:@"document_types"]; // Not supported yet by Onfido SDK`s

    ONFlowConfigBuilder *configBuilder = [ONFlowConfig builder];

    [configBuilder withToken:token];
    [configBuilder withApplicantId:applicantId];
    if([flowSteps containsObject: @"face"]) {
        [configBuilder withFaceStepOfVariant:ONFaceStepVariantPhoto];
    } else if([flowSteps containsObject: @"face_video"]) {
        [configBuilder withFaceStepOfVariant:ONFaceStepVariantVideo];
    }
    [configBuilder withDocumentStep];

    NSError *configError = NULL;
    ONFlowConfig *config = [configBuilder buildAndReturnError:&configError];

    if (configError == NULL) {
        ONFlow *onFlow = [[ONFlow alloc] initWithFlowConfiguration:config];

        [onFlow withResponseHandler:^(ONFlowResponse* response){
            [self handleOnFidoCallback: response :command.callbackId];
        }];

        NSError *runError = NULL;
        UIViewController *onfidoController = [onFlow runAndReturnError:&runError];

        if (runError == NULL) {
            [self.viewController presentViewController:onfidoController animated:YES completion:NULL];
        } else {
            [self showAlert:@"Error occured during Onfido flow. Look for details in console"];
        }
    } else
        [self handleConfigsError:configError :command.callbackId];
}

#pragma mark - "Private methods"

- (NSString*) buildDocumentJson: (NSArray*) documentsResult {

    NSMutableDictionary *documentKeyValue =  [[NSMutableDictionary alloc] initWithCapacity:2];

    ONDocumentResult* front = ((ONFlowResult*) documentsResult[0]).result;
    NSDictionary* frontKeyValue = [NSDictionary dictionaryWithObjectsAndKeys:
                                   front.id, @"id",
                                   front.side, @"side",
                                   front.type, @"type",
                                   nil];

    [documentKeyValue setObject:frontKeyValue forKey:@"front"];

    if (([documentsResult count] > 1 && [documentsResult objectAtIndex:1] !=nil)) {
        ONDocumentResult* back = ((ONFlowResult*) documentsResult[1]).result;
        NSDictionary* backKeyValue = [NSDictionary dictionaryWithObjectsAndKeys:
                                      back.id, @"id",
                                      back.side, @"side",
                                      back.type, @"type",
                                      nil];
        [documentKeyValue setObject:backKeyValue forKey:@"back"];
    }

    NSDictionary* resultKeyValue = [NSDictionary dictionaryWithObjectsAndKeys:
                                    documentKeyValue, @"document",
                                    nil];
    NSError* error;
    NSData* json = [NSJSONSerialization dataWithJSONObject:resultKeyValue options:NSJSONWritingPrettyPrinted error:&error];

    return [[NSString alloc]initWithData:json encoding:NSUTF8StringEncoding];
}

- (void)handleOnFidoCallback: (ONFlowResponse*) response : (id) callbackId {
    if(response.userCanceled) {
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT messageAsString:@"User exit"];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];
    } else if(response.results) {
        NSPredicate *documentResultPredicate = [NSPredicate predicateWithBlock:^BOOL(id flowResult, NSDictionary* bindings) {
            if(((ONFlowResult*)flowResult).type == ONFlowResultTypeDocument) {
                return YES;
            } else {
                return NO;
            }
        }];

        NSArray* flowWithDocumentResults = [response.results filteredArrayUsingPredicate:documentResultPredicate];
        NSString* documentJson = [self buildDocumentJson:flowWithDocumentResults];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:documentJson];
        [self.commandDelegate sendPluginResult:result callbackId:callbackId];

    } else if(response.error) {
        //something went wrong
        [self handleOnFlowError: response.error :callbackId];
    }
    [self.viewController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
}

- (void)handleConfigsError: (NSError*) error : (id) callbackId {
    NSString* errMsg;
    switch (error.code) {
        case ONFlowConfigErrorMissingToken:
            errMsg = @"No token provided";
            break;
        case ONFlowConfigErrorMissingApplicant:
            errMsg = @"No applicant provided";
            break;
        case ONFlowConfigErrorMissingSteps:
            errMsg = @"No steps provided";
            break;
        case ONFlowConfigErrorMultipleApplicants:
            errMsg = @"Failed to upload capture";
            break;
        default:
            errMsg = [NSString stringWithFormat:@"Unknown error occured. Code: %ld. Description: %@", error.code, error.description];
            break;
    }

    NSLog(@"%@", errMsg);
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errMsg];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];

    [self.viewController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
}

- (void)handleOnFlowError: (NSError*) error : (id) callbackId{
    NSString* errMsg;
    switch (error.code) {
        case ONFlowErrorCameraPermission:
            errMsg = @"Onfido sdk does not have camera permissions";
            break;
        case ONFlowErrorFailedToWriteToDisk:
            errMsg = @"Onfido sdk failed to save capture to disk. May be due to a lack of space";
            break;
        case ONFlowErrorMicrophonePermission:
            errMsg = @"Onfido sdk does not have microphone permissions";
            break;
        case ONFlowErrorUpload:
            errMsg = @"Failed to upload capture";
            break;
        case ONFlowErrorException:
            errMsg = [NSString stringWithFormat: @"Unexpected error occured. Code: %ld. Description: %@", error.code,
                      error.description];
            break;
        default:
            errMsg = [NSString stringWithFormat: @"Unknown error occured. Code: %ld. Description: %@", error.code,
                      error.description];
            break;
    }
    NSLog(@"%@", errMsg);
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errMsg];
    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (void)showAlert:(NSString*) msg {
    UIAlertController* alert = [UIAlertController alertControllerWithTitle:@"Error" message:msg preferredStyle:UIAlertControllerStyleAlert];

    UIAlertAction* defaultAction = [UIAlertAction
                                    actionWithTitle:@"OK"
                                    style:UIAlertActionStyleDefault
                                    handler:nil];

    [alert addAction:defaultAction];

    [self.viewController presentViewController:alert animated:YES completion:nil];
}

@end
