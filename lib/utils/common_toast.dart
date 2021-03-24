import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_plugin_record/widgets/custom_overlay.dart';

class CommonToast {
  static showView({
    BuildContext? context,
    String? msg,
    TextStyle? style,
    Widget? icon,
    Duration duration = const Duration(seconds: 1),
    int count = 3,
    Function? onTap,
  }) {
    OverlayEntry? overlayEntry;
    int _count = 0;

    void removeOverlay() {
      overlayEntry?.remove();
      overlayEntry = null;
    }

    if (overlayEntry == null) {
      overlayEntry = new OverlayEntry(builder: (content) {
        return Container(
          child: GestureDetector(
            onTap: () {
              if (onTap != null) {
                removeOverlay();
                onTap();
              }
            },
            child: CustomOverlay(
              icon: Column(
                children: [
                  Padding(
                    child: icon,
                    padding: const EdgeInsets.only(
                      bottom: 10.0,
                    ),
                  ),
                  Container(
//                      padding: EdgeInsets.only(right: 20, left: 20, top: 0),
                    child: Text(
                      msg ?? '',
                      style: style ??
                          TextStyle(
                            fontStyle: FontStyle.normal,
                            color: Colors.white,
                            fontSize: 16,
                          ),
                    ),
                  )
                ],
              ),
            ),
          ),
        );
      });
      Overlay.of(context!)!.insert(overlayEntry!);
      if (onTap != null) return;
      Timer.periodic(duration, (timer) {
        _count++;
        if (_count == count) {
          _count = 0;
          timer.cancel();
          removeOverlay();
        }
      });
    }
  }
}
