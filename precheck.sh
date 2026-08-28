#!/bin/bash
sbt clean scalafmt Test/scalafmt it/scalafmt coverage test it/test coverageReport