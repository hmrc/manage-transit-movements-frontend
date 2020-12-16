#!/bin/bash

echo ""
echo "Applying migration WhatDoYouWantToDo"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /whatDoYouWantToDo                       controllers.WhatDoYouWantToDoController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "whatDoYouWantToDo.title = whatDoYouWantToDo" >> ../conf/messages.en
echo "whatDoYouWantToDo.heading = whatDoYouWantToDo" >> ../conf/messages.en

echo "Migration WhatDoYouWantToDo completed"
