#import "FlutterPluginRecordPlugin.h"
#import <flutter_plugin_record/flutter_plugin_record-Swift.h>

@implementation FlutterPluginRecordPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterPluginRecordPlugin registerWithRegistrar:registrar];
}
@end
