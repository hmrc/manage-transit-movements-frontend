#!/bin/bash

echo ""
echo "Applying migration TechnicalDifficulties"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /technicalDifficulties                       controllers.TechnicalDifficultiesController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "technicalDifficulties.title = technicalDifficulties" >> ../conf/messages.en
echo "technicalDifficulties.heading = technicalDifficulties" >> ../conf/messages.en

echo "Migration TechnicalDifficulties completed"
