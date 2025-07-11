/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models

import base.SpecBase
import models.IE015.*

class IE015Spec extends SpecBase {

  "TransitOperation" - {
    "must deserialise" in {
      val xml = <TransitOperation>
        <LRN>GB8dd4bg3h38001087c695</LRN>
        <declarationType>T</declarationType>
        <additionalDeclarationType>A</additionalDeclarationType>
        <security>0</security>
        <reducedDatasetIndicator>0</reducedDatasetIndicator>
        <bindingItinerary>0</bindingItinerary>
      </TransitOperation>

      val result = TransitOperation.reads(xml)

      result mustEqual TransitOperation(
        additionalDeclarationType = "A"
      )
    }
  }

  "IE015" - {
    "must deserialise" in {
      val xml = <ncts:CC015C PhaseID="NCTS5.0" xmlns:ncts="http://ncts.dgtaxud.ec">
        <messageSender>token</messageSender>
        <messageRecipient>NTA.GB</messageRecipient>
        <preparationDateAndTime>{{currentDateTime}}</preparationDateAndTime>
        <messageIdentification>6Onxa3En</messageIdentification>
        <messageType>CC015C</messageType>
        <TransitOperation>
          <LRN>GB8dd4bg3h38001087c695</LRN>
          <declarationType>T</declarationType>
          <additionalDeclarationType>A</additionalDeclarationType>
          <security>0</security>
          <reducedDatasetIndicator>0</reducedDatasetIndicator>
          <bindingItinerary>0</bindingItinerary>
        </TransitOperation>
        <CustomsOfficeOfDeparture>
          <referenceNumber>GB000246</referenceNumber>
        </CustomsOfficeOfDeparture>
        <CustomsOfficeOfDestinationDeclared>
          <referenceNumber>XI000142</referenceNumber>
        </CustomsOfficeOfDestinationDeclared>
        <CustomsOfficeOfTransitDeclared>
          <sequenceNumber>1</sequenceNumber>
          <referenceNumber>XI000142</referenceNumber>
        </CustomsOfficeOfTransitDeclared>
        <HolderOfTheTransitProcedure>
          <identificationNumber>GB201909015000</identificationNumber>
        </HolderOfTheTransitProcedure>
        <Guarantee>
          <sequenceNumber>1</sequenceNumber>
          <guaranteeType>1</guaranteeType>
          <GuaranteeReference>
            <sequenceNumber>1</sequenceNumber>
            <GRN>24GB0000010000295</GRN>
            <accessCode>AC01</accessCode>
            <amountToBeCovered>10</amountToBeCovered>
            <currency>GBP</currency>
          </GuaranteeReference>
        </Guarantee>
        <Consignment>
          <countryOfDispatch>XI</countryOfDispatch>
          <countryOfDestination>XI</countryOfDestination>
          <containerIndicator>1</containerIndicator>
          <grossMass>1000</grossMass>
          <referenceNumberUCR>AB1234</referenceNumberUCR>
          <Consignor>
            <identificationNumber>GB201909015000</identificationNumber>
          </Consignor>
          <Consignee>
            <identificationNumber>XI985524247819</identificationNumber>
          </Consignee>
          <TransportEquipment>
            <sequenceNumber>1</sequenceNumber>
            <containerIdentificationNumber>WGPCGR</containerIdentificationNumber>
            <numberOfSeals>1</numberOfSeals>
            <Seal>
              <sequenceNumber>1</sequenceNumber>
              <identifier>1234</identifier>
            </Seal>
            <GoodsReference>
              <sequenceNumber>1</sequenceNumber>
              <declarationGoodsItemNumber>1</declarationGoodsItemNumber>
            </GoodsReference>
          </TransportEquipment>
          <LocationOfGoods>
            <typeOfLocation>C</typeOfLocation>
            <qualifierOfIdentification>U</qualifierOfIdentification>
            <UNLocode>GBDVR</UNLocode>
          </LocationOfGoods>
          <DepartureTransportMeans>
            <sequenceNumber>1</sequenceNumber>
            <typeOfIdentification>30</typeOfIdentification>
            <identificationNumber>NC15REG</identificationNumber>
            <nationality>GB</nationality>
          </DepartureTransportMeans>
          <CountryOfRoutingOfConsignment>
            <sequenceNumber>1</sequenceNumber>
            <country>XI</country>
          </CountryOfRoutingOfConsignment>
          <PlaceOfLoading>
            <UNLocode>GBDVR</UNLocode>
            <country>GB</country>
            <location>Dover</location>
          </PlaceOfLoading>
          <PreviousDocument>
            <sequenceNumber>1</sequenceNumber>
            <type>C512</type>
            <referenceNumber>PrevL0001</referenceNumber>
            <complementOfInformation>AddInf0</complementOfInformation>
          </PreviousDocument>
          <TransportDocument>
            <sequenceNumber>1</sequenceNumber>
            <type>N703</type>
            <referenceNumber>TransportBill</referenceNumber>
          </TransportDocument>
          <HouseConsignment>
            <sequenceNumber>1</sequenceNumber>
            <grossMass>1000</grossMass>
            <ConsignmentItem>
              <goodsItemNumber>1</goodsItemNumber>
              <declarationGoodsItemNumber>1</declarationGoodsItemNumber>
              <Commodity>
                <descriptionOfGoods>Toys</descriptionOfGoods>
                <CommodityCode>
                  <harmonizedSystemSubHeadingCode>392690</harmonizedSystemSubHeadingCode>
                </CommodityCode>
                <GoodsMeasure>
                  <grossMass>1000</grossMass>
                  <netMass>950</netMass>
                  <supplementaryUnits>11</supplementaryUnits>
                </GoodsMeasure>
              </Commodity>
              <Packaging>
                <sequenceNumber>1</sequenceNumber>
                <typeOfPackages>BX</typeOfPackages>
                <numberOfPackages>1</numberOfPackages>
                <shippingMarks>GB box</shippingMarks>
              </Packaging>
              <SupportingDocument>
                <sequenceNumber>1</sequenceNumber>
                <type>C651</type>
                <referenceNumber>Supp0101</referenceNumber>
                <documentLineItemNumber>10</documentLineItemNumber>
                <complementOfInformation>SupportD0C</complementOfInformation>
              </SupportingDocument>
            </ConsignmentItem>
          </HouseConsignment>
        </Consignment>
      </ncts:CC015C>

      val result = IE015.reads(xml)

      result mustEqual IE015(
        transitOperation = TransitOperation(
          additionalDeclarationType = "A"
        )
      )
    }
  }
}
