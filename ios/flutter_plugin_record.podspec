#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
#use_frameworks!
Pod::Spec.new do |s|
  s.name             = 'flutter_plugin_record'
  s.version          = '0.0.1'
  s.summary          = 'A new Flutter plugin.'
  s.description      = <<-DESC
A new Flutter plugin.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
#  s.swift_version = '4.2'
  s.source_files = 'Classes/**/*.{h,m}'
  s.public_header_files = 'Classes/**/*.h'
#  s.vendored_libraries = 'Classes/libopencore-amrnb.a'
  s.dependency 'Flutter'
  s.framework  = "AVFoundation"
  s.ios.deployment_target = '8.0'
end

