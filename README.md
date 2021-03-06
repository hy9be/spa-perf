# spa-perf
"Single Page Application - Performance“
A chrome webdriver based framework to measure the performance and responsiveness of UI on single page web app

## Introduction
There are several existing libraries and tools to monitor the page loading performance of web application. But for single page app, initial loading of the page is not the full story. After the page is loaded, performance of subsequent user actions is also a very important metric.

Thus we come up with this framework to support the in-depth performance measurement (mostly from Chrome performance logging and tracing data) of the actions on single page apps.

The framework will be implemented in Java first. We will port it to other languages like node.js, python, and etc.

## Reference
Performance logging with chrome webdriver
https://sites.google.com/a/chromium.org/chromedriver/logging/performance-log
https://github.com/cabbiepete/browser-perf/commit/046f65f02db418c17ec2d59c43abcc0de642a60f