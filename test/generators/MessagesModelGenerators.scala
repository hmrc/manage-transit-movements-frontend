/*
 * Copyright 2023 HM Revenue & Customs
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

package generators

import generated._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen.const
import org.scalacheck.{Arbitrary, Gen}
import scalaxb.XMLCalendar

import javax.xml.datatype.XMLGregorianCalendar

trait MessagesModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryCC009CType: Arbitrary[CC009CType] =
    Arbitrary {
      for {
        messageSequence1            <- arbitrary[MESSAGESequence]
        transitOperation            <- arbitrary[TransitOperationType03]
        invalidation                <- arbitrary[InvalidationType01]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType03]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType13]
      } yield CC009CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        Invalidation = invalidation,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC015CType: Arbitrary[CC015CType] =
    Arbitrary {
      for {
        messageSequence1                         <- arbitrary[MESSAGESequence]
        transitOperation                         <- arbitrary[TransitOperationType06]
        customsOfficeOfDeparture                 <- arbitrary[CustomsOfficeOfDepartureType03]
        customsOfficeOfDestinationDeclaredType01 <- arbitrary[CustomsOfficeOfDestinationDeclaredType01]
        holderOfTheTransitProcedure              <- arbitrary[HolderOfTheTransitProcedureType14]
        consignment                              <- arbitrary[ConsignmentType20]
      } yield CC015CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        Authorisation = Nil,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        CustomsOfficeOfDestinationDeclared = customsOfficeOfDestinationDeclaredType01,
        CustomsOfficeOfTransitDeclared = Nil,
        CustomsOfficeOfExitForTransitDeclared = Nil,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        Representative = None,
        Guarantee = Nil,
        Consignment = consignment,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC035CType: Arbitrary[CC035CType] =
    Arbitrary {
      for {
        messageSequence1                   <- arbitrary[MESSAGESequence]
        transitOperation                   <- arbitrary[TransitOperationType48]
        recoveryNotification               <- arbitrary[RecoveryNotificationType]
        customsOfficeOfDeparture           <- arbitrary[CustomsOfficeOfDepartureType03]
        customsOfficeOfRecoveryAtDeparture <- arbitrary[CustomsOfficeOfRecoveryAtDepartureType01]
        holderOfTheTransitProcedure        <- arbitrary[HolderOfTheTransitProcedureType20]
        guarantor                          <- Gen.option(arbitrary[GuarantorType06])
      } yield CC035CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        RecoveryNotification = recoveryNotification,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        CustomsOfficeOfRecoveryAtDeparture = customsOfficeOfRecoveryAtDeparture,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        Guarantor = guarantor,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC051CType: Arbitrary[CC051CType] =
    Arbitrary {
      for {
        messageSequence1            <- arbitrary[MESSAGESequence]
        transitOperation            <- arbitrary[TransitOperationType18]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType03]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType15]
        representative              <- Gen.option(arbitrary[RepresentativeType03])
      } yield CC051CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        Representative = representative,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC055CType: Arbitrary[CC055CType] =
    Arbitrary {
      for {
        messageSequence1            <- arbitrary[MESSAGESequence]
        transitOperation            <- arbitrary[TransitOperationType48]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType03]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType07]
        guaranteeReferences         <- arbitrary[Seq[GuaranteeReferenceType08]]
      } yield CC055CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        GuaranteeReference = guaranteeReferences,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC056CType: Arbitrary[CC056CType] =
    Arbitrary {
      for {
        messageSequence1            <- arbitrary[MESSAGESequence]
        transitOperation            <- arbitrary[TransitOperationType20]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType03]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType08]
        representative              <- Gen.option(arbitrary[RepresentativeType01])
        functionalErrors            <- arbitrary[Seq[FunctionalErrorType04]]
      } yield CC056CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        Representative = representative,
        FunctionalError = functionalErrors,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC057CType: Arbitrary[CC057CType] =
    Arbitrary {
      for {
        messageSequence1                 <- arbitrary[MESSAGESequence]
        transitOperation                 <- arbitrary[TransitOperationType21]
        customsOfficeOfDestinationActual <- arbitrary[CustomsOfficeOfDestinationActualType03]
        traderAtDestination              <- arbitrary[TraderAtDestinationType03]
        functionalErrors                 <- arbitrary[Seq[FunctionalErrorType04]]
      } yield CC057CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        CustomsOfficeOfDestinationActual = customsOfficeOfDestinationActual,
        TraderAtDestination = traderAtDestination,
        FunctionalError = functionalErrors,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC060CType: Arbitrary[CC060CType] =
    Arbitrary {
      for {
        messageSequence1            <- arbitrary[MESSAGESequence]
        transitOperation            <- arbitrary[TransitOperationType22]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType03]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType13]
        representative              <- Gen.option(arbitrary[RepresentativeType04])
        typeOfControls              <- arbitrary[Seq[TypeOfControlsType]]
        requestedDocuments          <- arbitrary[Seq[RequestedDocumentType]]
      } yield CC060CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        Representative = representative,
        TypeOfControls = typeOfControls,
        RequestedDocument = requestedDocuments,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC182CType: Arbitrary[CC182CType] =
    Arbitrary {
      for {
        messageSequence1                    <- arbitrary[MESSAGESequence]
        transitOperation                    <- arbitrary[TransitOperationType47]
        customsOfficeOfDeparture            <- arbitrary[CustomsOfficeOfDepartureType03]
        customsOfficeOfIncidentRegistration <- arbitrary[CustomsOfficeOfIncidentRegistrationType02]
        consignment                         <- arbitrary[ConsignmentType22]
      } yield CC182CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        CustomsOfficeOfIncidentRegistration = customsOfficeOfIncidentRegistration,
        Consignment = consignment,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryMESSAGESequence: Arbitrary[MESSAGESequence] =
    Arbitrary {
      for {
        messageSender                   <- nonEmptyString
        messagE_1Sequence2              <- arbitrary[MESSAGE_1Sequence]
        messagE_TYPESequence3           <- arbitrary[MESSAGE_TYPESequence]
        correlatioN_IDENTIFIERSequence4 <- arbitrary[CORRELATION_IDENTIFIERSequence]
      } yield MESSAGESequence(
        messageSender = messageSender,
        messagE_1Sequence2 = messagE_1Sequence2,
        messagE_TYPESequence3 = messagE_TYPESequence3,
        correlatioN_IDENTIFIERSequence4 = correlatioN_IDENTIFIERSequence4
      )
    }

  implicit lazy val arbitraryMESSAGE_1Sequence: Arbitrary[MESSAGE_1Sequence] =
    Arbitrary {
      for {
        messageRecipient       <- nonEmptyString
        preparationDateAndTime <- arbitrary[XMLGregorianCalendar]
        messageIdentification  <- nonEmptyString
      } yield MESSAGE_1Sequence(
        messageRecipient = messageRecipient,
        preparationDateAndTime = preparationDateAndTime,
        messageIdentification = messageIdentification
      )
    }

  implicit lazy val arbitraryMESSAGE_TYPESequence: Arbitrary[MESSAGE_TYPESequence] =
    Arbitrary {
      MESSAGE_TYPESequence(CC043C)
    }

  implicit lazy val arbitraryCORRELATION_IDENTIFIERSequence: Arbitrary[CORRELATION_IDENTIFIERSequence] =
    Arbitrary {
      for {
        correlationIdentifier <- Gen.option(nonEmptyString)
      } yield CORRELATION_IDENTIFIERSequence(
        correlationIdentifier = correlationIdentifier
      )
    }

  implicit lazy val arbitraryConsignmentType20: Arbitrary[ConsignmentType20] =
    Arbitrary {
      for {
        grossMass <- arbitrary[BigDecimal]
      } yield ConsignmentType20(
        countryOfDispatch = None,
        countryOfDestination = None,
        containerIndicator = None,
        inlandModeOfTransport = None,
        modeOfTransportAtTheBorder = None,
        grossMass = grossMass,
        referenceNumberUCR = None,
        Carrier = None,
        Consignor = None,
        Consignee = None,
        AdditionalSupplyChainActor = Nil,
        TransportEquipment = Nil,
        LocationOfGoods = None,
        DepartureTransportMeans = Nil,
        CountryOfRoutingOfConsignment = Nil,
        ActiveBorderTransportMeans = Nil,
        PlaceOfLoading = None,
        PlaceOfUnloading = None,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil,
        TransportCharges = None,
        HouseConsignment = Nil
      )
    }

  implicit lazy val arbitraryConsignmentType22: Arbitrary[ConsignmentType22] =
    Arbitrary {
      for {
        incident <- arbitrary[Seq[IncidentType03]]
      } yield ConsignmentType22(
        Incident = incident
      )
    }

  implicit lazy val arbitraryTransitOperationType03: Arbitrary[TransitOperationType03] =
    Arbitrary {
      for {
        lrn <- Gen.option(nonEmptyString)
        mrn <- Gen.option(nonEmptyString)
      } yield TransitOperationType03(
        LRN = lrn,
        MRN = mrn
      )
    }

  implicit lazy val arbitraryTransitOperationType06: Arbitrary[TransitOperationType06] =
    Arbitrary {
      for {
        lrn                       <- nonEmptyString
        declarationType           <- nonEmptyString
        additionalDeclarationType <- nonEmptyString
        security                  <- nonEmptyString
        reducedDatasetIndicator   <- arbitrary[Flag]
        bindingItinerary          <- arbitrary[Flag]
      } yield TransitOperationType06(
        LRN = lrn,
        declarationType = declarationType,
        additionalDeclarationType = additionalDeclarationType,
        TIRCarnetNumber = None,
        presentationOfTheGoodsDateAndTime = None,
        security = security,
        reducedDatasetIndicator = reducedDatasetIndicator,
        specificCircumstanceIndicator = None,
        communicationLanguageAtDeparture = None,
        bindingItinerary = bindingItinerary,
        limitDate = None
      )
    }

  implicit lazy val arbitraryTransitOperationType14: Arbitrary[TransitOperationType14] =
    Arbitrary {
      for {
        mrn                       <- nonEmptyString
        declarationType           <- Gen.option(nonEmptyString)
        declarationAcceptanceDate <- Gen.option(arbitrary[XMLGregorianCalendar])
        security                  <- nonEmptyString
        reducedDatasetIndicator   <- arbitrary[Flag]
      } yield TransitOperationType14(
        MRN = mrn,
        declarationType = declarationType,
        declarationAcceptanceDate = declarationAcceptanceDate,
        security = security,
        reducedDatasetIndicator = reducedDatasetIndicator
      )
    }

  implicit lazy val arbitraryTransitOperationType18: Arbitrary[TransitOperationType18] =
    Arbitrary {
      for {
        mrn                              <- nonEmptyString
        declarationSubmissionDateAndTime <- arbitrary[XMLGregorianCalendar]
        noReleaseMotivationCode          <- nonEmptyString
        noReleaseMotivationText          <- nonEmptyString
      } yield TransitOperationType18(
        MRN = mrn,
        declarationSubmissionDateAndTime = declarationSubmissionDateAndTime,
        noReleaseMotivationCode = noReleaseMotivationCode,
        noReleaseMotivationText = noReleaseMotivationText
      )
    }

  implicit lazy val arbitraryTransitOperationType20: Arbitrary[TransitOperationType20] =
    Arbitrary {
      for {
        lrn                   <- Gen.option(nonEmptyString)
        mrn                   <- Gen.option(nonEmptyString)
        businessRejectionType <- nonEmptyString
        rejectionDateAndTime  <- arbitrary[XMLGregorianCalendar]
        rejectionCode         <- nonEmptyString
        rejectionReason       <- Gen.option(nonEmptyString)
      } yield TransitOperationType20(
        LRN = lrn,
        MRN = mrn,
        businessRejectionType = businessRejectionType,
        rejectionDateAndTime = rejectionDateAndTime,
        rejectionCode = rejectionCode,
        rejectionReason = rejectionReason
      )
    }

  implicit lazy val arbitraryTransitOperationType21: Arbitrary[TransitOperationType21] =
    Arbitrary {
      for {
        mrn                   <- nonEmptyString
        businessRejectionType <- nonEmptyString
        rejectionDateAndTime  <- arbitrary[XMLGregorianCalendar]
        rejectionCode         <- nonEmptyString
        rejectionReason       <- Gen.option(nonEmptyString)
      } yield TransitOperationType21(
        MRN = mrn,
        businessRejectionType = businessRejectionType,
        rejectionDateAndTime = rejectionDateAndTime,
        rejectionCode = rejectionCode,
        rejectionReason = rejectionReason
      )
    }

  implicit lazy val arbitraryTransitOperationType22: Arbitrary[TransitOperationType22] =
    Arbitrary {
      for {
        lrn                            <- Gen.option(nonEmptyString)
        mrn                            <- Gen.option(nonEmptyString)
        controlNotificationDateAndTime <- arbitrary[XMLGregorianCalendar]
        notificationType               <- nonEmptyString
      } yield TransitOperationType22(
        LRN = lrn,
        MRN = mrn,
        controlNotificationDateAndTime = controlNotificationDateAndTime,
        notificationType = notificationType
      )
    }

  implicit lazy val arbitraryTransitOperationType47: Arbitrary[TransitOperationType47] =
    Arbitrary {
      for {
        mrn                             <- nonEmptyString
        incidentNotificationDateAndTime <- arbitrary[XMLGregorianCalendar]
      } yield TransitOperationType47(
        MRN = mrn,
        incidentNotificationDateAndTime = incidentNotificationDateAndTime
      )
    }

  implicit lazy val arbitraryTransitOperationType48: Arbitrary[TransitOperationType48] =
    Arbitrary {
      for {
        mrn                       <- nonEmptyString
        declarationAcceptanceDate <- arbitrary[XMLGregorianCalendar]
      } yield TransitOperationType48(
        MRN = mrn,
        declarationAcceptanceDate = declarationAcceptanceDate
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfDestinationActualType03: Arbitrary[CustomsOfficeOfDestinationActualType03] =
    Arbitrary {
      for {
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfDestinationActualType03(
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfDepartureType03: Arbitrary[CustomsOfficeOfDepartureType03] =
    Arbitrary {
      for {
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfDepartureType03(
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfRecoveryAtDepartureType01: Arbitrary[CustomsOfficeOfRecoveryAtDepartureType01] =
    Arbitrary {
      for {
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfRecoveryAtDepartureType01(
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfDestinationDeclaredType01: Arbitrary[CustomsOfficeOfDestinationDeclaredType01] =
    Arbitrary {
      for {
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfDestinationDeclaredType01(
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType06: Arbitrary[HolderOfTheTransitProcedureType06] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- nonEmptyString
        address                       <- arbitrary[AddressType10]
      } yield HolderOfTheTransitProcedureType06(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType07: Arbitrary[HolderOfTheTransitProcedureType07] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(nonEmptyString)
        name                 <- Gen.option(nonEmptyString)
        address              <- Gen.option(arbitrary[AddressType03])
      } yield HolderOfTheTransitProcedureType07(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType08: Arbitrary[HolderOfTheTransitProcedureType08] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
        address                       <- Gen.option(arbitrary[AddressType07])
      } yield HolderOfTheTransitProcedureType08(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType13: Arbitrary[HolderOfTheTransitProcedureType13] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
        address                       <- Gen.option(arbitrary[AddressType07])
        contactPerson                 <- Gen.option(arbitrary[ContactPersonType04])
      } yield HolderOfTheTransitProcedureType13(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = address,
        ContactPerson = contactPerson
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType14: Arbitrary[HolderOfTheTransitProcedureType14] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
        address                       <- Gen.option(arbitrary[AddressType17])
        contactPerson                 <- Gen.option(arbitrary[ContactPersonType05])
      } yield HolderOfTheTransitProcedureType14(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = address,
        ContactPerson = contactPerson
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType15: Arbitrary[HolderOfTheTransitProcedureType15] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
        address                       <- Gen.option(arbitrary[AddressType15])
      } yield HolderOfTheTransitProcedureType15(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType20: Arbitrary[HolderOfTheTransitProcedureType20] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
        address                       <- Gen.option(arbitrary[AddressType07])
      } yield HolderOfTheTransitProcedureType20(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryRepresentativeType01: Arbitrary[RepresentativeType01] =
    Arbitrary {
      for {
        identificationNumber <- nonEmptyString
        status               <- nonEmptyString
      } yield RepresentativeType01(
        identificationNumber = identificationNumber,
        status = status
      )
    }

  implicit lazy val arbitraryRepresentativeType03: Arbitrary[RepresentativeType03] =
    Arbitrary {
      for {
        identificationNumber <- nonEmptyString
        contactPerson        <- Gen.option(arbitrary[ContactPersonType01])
      } yield RepresentativeType03(
        identificationNumber = identificationNumber,
        ContactPerson = contactPerson
      )
    }

  implicit lazy val arbitraryRepresentativeType04: Arbitrary[RepresentativeType04] =
    Arbitrary {
      for {
        identificationNumber <- nonEmptyString
        status               <- nonEmptyString
        contactPerson        <- Gen.option(arbitrary[ContactPersonType04])
      } yield RepresentativeType04(
        identificationNumber = identificationNumber,
        status = status,
        ContactPerson = contactPerson
      )
    }

  implicit lazy val arbitraryTraderAtDestinationType03: Arbitrary[TraderAtDestinationType03] =
    Arbitrary {
      for {
        identificationNumber <- nonEmptyString
      } yield TraderAtDestinationType03(
        identificationNumber = identificationNumber
      )
    }

  implicit lazy val arbitraryCTLControlType: Arbitrary[CTLControlType] =
    Arbitrary {
      for {
        continueUnloading <- positiveBigInts
      } yield CTLControlType(
        continueUnloading = continueUnloading
      )
    }

  implicit lazy val arbitraryConsignmentType05: Arbitrary[ConsignmentType05] =
    Arbitrary {
      for {
        countryOfDestination  <- Gen.option(nonEmptyString)
        containerIndicator    <- arbitrary[Flag]
        inlandModeOfTransport <- Gen.option(nonEmptyString)
        grossMass             <- Gen.option(positiveBigDecimals)
        consignor             <- Gen.option(arbitrary[ConsignorType05])
        consignee             <- Gen.option(arbitrary[ConsigneeType04])
        transportEquipment    <- arbitrary[Seq[TransportEquipmentType05]]
        incidents             <- arbitrary[Seq[IncidentType04]]
      } yield ConsignmentType05(
        countryOfDestination = countryOfDestination,
        containerIndicator = containerIndicator,
        inlandModeOfTransport = inlandModeOfTransport,
        grossMass = grossMass,
        Consignor = consignor,
        Consignee = consignee,
        TransportEquipment = transportEquipment,
        DepartureTransportMeans = Nil,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil,
        Incident = incidents,
        HouseConsignment = Nil
      )
    }

  implicit lazy val arbitraryHouseConsignmentType04: Arbitrary[HouseConsignmentType04] =
    Arbitrary {
      for {
        sequenceNumber          <- nonEmptyString
        grossMass               <- positiveBigDecimals
        consignor               <- Gen.option(arbitrary[ConsignorType06])
        consignee               <- Gen.option(arbitrary[ConsigneeType04])
        departureTransportMeans <- arbitrary[Seq[DepartureTransportMeansType02]]
        consignmentItems        <- arbitrary[Seq[ConsignmentItemType04]]
        securityIndicator       <- Gen.some(nonEmptyString)
      } yield HouseConsignmentType04(
        sequenceNumber = sequenceNumber,
        grossMass = grossMass,
        securityIndicatorFromExportDeclaration = securityIndicator,
        Consignor = consignor,
        Consignee = consignee,
        DepartureTransportMeans = departureTransportMeans,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil,
        ConsignmentItem = consignmentItems
      )
    }

  implicit lazy val arbitraryConsignorType05: Arbitrary[ConsignorType05] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(nonEmptyString)
        name                 <- Gen.option(nonEmptyString)
        address              <- Gen.option(arbitrary[AddressType07])
      } yield ConsignorType05(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryConsigneeType03: Arbitrary[ConsigneeType03] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(nonEmptyString)
        name                 <- Gen.option(nonEmptyString)
        address              <- Gen.option(arbitrary[AddressType09])
      } yield ConsigneeType03(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryConsigneeType04: Arbitrary[ConsigneeType04] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(nonEmptyString)
        name                 <- Gen.option(nonEmptyString)
        address              <- Gen.option(arbitrary[AddressType07])
      } yield ConsigneeType04(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryConsignorType06: Arbitrary[ConsignorType06] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(nonEmptyString)
        name                 <- Gen.option(nonEmptyString)
        address              <- Gen.option(arbitrary[AddressType07])
      } yield ConsignorType06(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryDepartureTransportMeansType02: Arbitrary[DepartureTransportMeansType02] =
    Arbitrary {
      for {
        sequenceNumber       <- nonEmptyString
        typeOfIdentification <- nonEmptyString
        identificationNumber <- nonEmptyString
        nationality          <- nonEmptyString
      } yield DepartureTransportMeansType02(
        sequenceNumber = sequenceNumber,
        typeOfIdentification = typeOfIdentification,
        identificationNumber = identificationNumber,
        nationality = nationality
      )
    }

  implicit lazy val arbitraryTransportEquipmentType05: Arbitrary[TransportEquipmentType05] =
    Arbitrary {
      for {
        sequenceNumber                <- nonEmptyString
        containerIdentificationNumber <- Gen.option(nonEmptyString)
        seals                         <- arbitrary[Seq[SealType04]]
        goodsReferences               <- arbitrary[Seq[GoodsReferenceType02]]
      } yield TransportEquipmentType05(
        sequenceNumber = sequenceNumber,
        containerIdentificationNumber = containerIdentificationNumber,
        numberOfSeals = seals.length,
        Seal = seals,
        GoodsReference = goodsReferences
      )
    }

  implicit lazy val arbitraryTransportEquipmentType07: Arbitrary[TransportEquipmentType07] =
    Arbitrary {
      for {
        sequenceNumber                <- nonEmptyString
        containerIdentificationNumber <- Gen.option(nonEmptyString)
        seals                         <- arbitrary[Seq[SealType04]]
        numberOfSeals                 <- Gen.option(positiveBigInts)
        goodsReferences               <- arbitrary[Seq[GoodsReferenceType01]]
      } yield TransportEquipmentType07(
        sequenceNumber = sequenceNumber,
        containerIdentificationNumber = containerIdentificationNumber,
        numberOfSeals = numberOfSeals,
        Seal = seals,
        GoodsReference = goodsReferences
      )
    }

  implicit lazy val arbitraryAdditionalReferenceType02: Arbitrary[AdditionalReferenceType02] =
    Arbitrary {
      for {
        sequenceNumber <- nonEmptyString
        typeVal        <- nonEmptyString
        refNum         <- Gen.option(nonEmptyString)
      } yield AdditionalReferenceType02(
        sequenceNumber = sequenceNumber,
        typeValue = typeVal,
        referenceNumber = refNum
      )
    }

  implicit lazy val arbitraryAdditionalReferenceType03: Arbitrary[AdditionalReferenceType03] =
    Arbitrary {
      for {
        sequenceNumber <- nonEmptyString
        typeVal        <- nonEmptyString
        refNum         <- Gen.option(nonEmptyString)
      } yield AdditionalReferenceType03(
        sequenceNumber = sequenceNumber,
        typeValue = typeVal,
        referenceNumber = refNum
      )
    }

  implicit lazy val arbitraryAdditionalInformationType02: Arbitrary[AdditionalInformationType02] =
    Arbitrary {
      for {
        sequenceNumber <- nonEmptyString
        code           <- nonEmptyString
        text           <- Gen.option(nonEmptyString)
      } yield AdditionalInformationType02(
        sequenceNumber = sequenceNumber,
        code = code,
        text = text
      )
    }

  implicit lazy val arbitrarySealType04: Arbitrary[SealType04] =
    Arbitrary {
      for {
        sequenceNumber <- nonEmptyString
        identifier     <- nonEmptyString
      } yield SealType04(
        sequenceNumber = sequenceNumber,
        identifier = identifier
      )
    }

  implicit lazy val arbitraryPackageType04: Arbitrary[PackagingType02] =
    Arbitrary {
      for {
        sequenceNumber   <- nonEmptyString
        typeOfPackages   <- nonEmptyString
        numberOfPackages <- Gen.option(positiveBigInts)
        shippingMarks    <- Gen.option(nonEmptyString)

      } yield PackagingType02(
        sequenceNumber = sequenceNumber,
        typeOfPackages,
        numberOfPackages,
        shippingMarks
      )
    }

  implicit lazy val arbitraryGoodsReferenceType02: Arbitrary[GoodsReferenceType02] =
    Arbitrary {
      for {
        sequenceNumber             <- nonEmptyString
        declarationGoodsItemNumber <- positiveBigInts
      } yield GoodsReferenceType02(
        sequenceNumber = sequenceNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber
      )
    }

  implicit lazy val arbitraryGoodsReferenceType01: Arbitrary[GoodsReferenceType01] =
    Arbitrary {
      for {
        sequenceNumber             <- nonEmptyString
        declarationGoodsItemNumber <- positiveBigInts
      } yield GoodsReferenceType01(
        sequenceNumber = sequenceNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber
      )
    }

  implicit lazy val arbitraryCommodityType08: Arbitrary[CommodityType08] =
    Arbitrary {
      for {
        descriptionOfGoods <- nonEmptyString
        cusCode            <- Gen.option(nonEmptyString)
        commodityCode      <- Gen.option(arbitrary[CommodityCodeType05])
        dangerousGoods     <- arbitrary[Seq[DangerousGoodsType01]]
        goodsMeasure       <- Gen.option(arbitrary[GoodsMeasureType03])
      } yield CommodityType08(
        descriptionOfGoods = descriptionOfGoods,
        cusCode = cusCode,
        CommodityCode = commodityCode,
        DangerousGoods = dangerousGoods,
        GoodsMeasure = goodsMeasure
      )
    }

  implicit lazy val arbitraryCommodityCodeType05: Arbitrary[CommodityCodeType05] =
    Arbitrary {
      for {
        harmonizedSystemSubHeadingCode <- nonEmptyString
        combinedNomenclatureCode       <- Gen.option(nonEmptyString)
      } yield CommodityCodeType05(
        harmonizedSystemSubHeadingCode = harmonizedSystemSubHeadingCode,
        combinedNomenclatureCode = combinedNomenclatureCode
      )
    }

  implicit lazy val arbitraryDangerousGoodsType01: Arbitrary[DangerousGoodsType01] =
    Arbitrary {
      for {
        sequenceNumber <- nonEmptyString
        unNumber       <- nonEmptyString
      } yield DangerousGoodsType01(
        sequenceNumber = sequenceNumber,
        UNNumber = unNumber
      )
    }

  implicit lazy val arbitraryGoodsMeasureType03: Arbitrary[GoodsMeasureType03] =
    Arbitrary {
      for {
        grossMass <- positiveBigDecimals
        netMass   <- Gen.option(positiveBigDecimals)
      } yield GoodsMeasureType03(
        grossMass = grossMass,
        netMass = netMass
      )
    }

  implicit lazy val arbitraryEndorsement03: Arbitrary[generated.EndorsementType03] = Arbitrary {
    for {
      date      <- arbitraryXMLGregorianCalendar.arbitrary
      authority <- nonEmptyString
      place     <- nonEmptyString
      country   <- nonEmptyString
    } yield EndorsementType03(date, authority, place, country)
  }

  implicit lazy val arbTranshipment02: Arbitrary[TranshipmentType02] = Arbitrary {
    for {
      flag            <- arbitraryFlag.arbitrary
      sequenceNumber  <- nonEmptyString
      typeValue       <- nonEmptyString
      referenceNumber <- nonEmptyString
    } yield TranshipmentType02(flag, TransportMeansType02(sequenceNumber, typeValue, referenceNumber))
  }

  implicit lazy val arbitraryIncidentType03: Arbitrary[generated.IncidentType03] =
    Arbitrary {
      for {
        sequenceNumber     <- nonEmptyString
        code               <- nonEmptyString
        text               <- nonEmptyString
        loc                <- arbitraryLocationType02.arbitrary
        endorsement        <- arbitraryEndorsement03.arbitrary
        transportEquipment <- arbitrary[Seq[TransportEquipmentType07]]
        transhipment       <- arbitrary[TranshipmentType02]
      } yield generated.IncidentType03(
        sequenceNumber = sequenceNumber,
        code = code,
        text = text,
        Endorsement = Some(endorsement),
        Location = loc,
        TransportEquipment = transportEquipment,
        Transhipment = Some(transhipment)
      )
    }

  implicit lazy val arbitraryIncidentType04: Arbitrary[generated.IncidentType04] =
    Arbitrary {
      for {
        sequenceNumber <- nonEmptyString
        code           <- nonEmptyString
        text           <- nonEmptyString
        loc            <- arbitraryLocationType02.arbitrary

      } yield generated.IncidentType04(
        sequenceNumber = sequenceNumber,
        code = code,
        text = text,
        Endorsement = None,
        Location = loc,
        TransportEquipment = Nil,
        Transhipment = None
      )
    }

  implicit lazy val arbitraryConsignmentItemType04: Arbitrary[ConsignmentItemType04] =
    Arbitrary {
      for {
        goodsItemNumber            <- nonEmptyString
        declarationGoodsItemNumber <- positiveBigInts
        commodity                  <- arbitrary[CommodityType08]
      } yield ConsignmentItemType04(
        goodsItemNumber = goodsItemNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber,
        declarationType = None,
        countryOfDestination = None,
        Consignee = None,
        Commodity = commodity,
        Packaging = Nil,
        PreviousDocument = Nil,
        SupportingDocument = Nil,
        TransportDocument = Nil,
        AdditionalReference = Nil,
        AdditionalInformation = Nil
      )
    }

  implicit lazy val arbitraryAddressType03: Arbitrary[AddressType03] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType03(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType07: Arbitrary[AddressType07] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType07(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType09: Arbitrary[AddressType09] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType09(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType10: Arbitrary[AddressType10] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType10(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType15: Arbitrary[AddressType15] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType15(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType16: Arbitrary[AddressType16] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- Gen.oneOf(CountryCodesCustomsOfficeLists.values)
      } yield AddressType16(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType17: Arbitrary[AddressType17] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType17(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType18: Arbitrary[AddressType18] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
      } yield AddressType18(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city
      )
    }

  implicit lazy val arbitraryContactPersonType01: Arbitrary[ContactPersonType01] =
    Arbitrary {
      for {
        name         <- nonEmptyString
        phoneNumber  <- nonEmptyString
        eMailAddress <- Gen.option(nonEmptyString)
      } yield ContactPersonType01(
        name = name,
        phoneNumber = phoneNumber,
        eMailAddress = eMailAddress
      )
    }

  implicit lazy val arbitraryContactPersonType04: Arbitrary[ContactPersonType04] =
    Arbitrary {
      for {
        name         <- nonEmptyString
        phoneNumber  <- nonEmptyString
        eMailAddress <- Gen.option(nonEmptyString)
      } yield ContactPersonType04(
        name = name,
        phoneNumber = phoneNumber,
        eMailAddress = eMailAddress
      )
    }

  implicit lazy val arbitraryContactPersonType05: Arbitrary[ContactPersonType05] =
    Arbitrary {
      for {
        name         <- nonEmptyString
        phoneNumber  <- nonEmptyString
        eMailAddress <- Gen.option(nonEmptyString)
      } yield ContactPersonType05(
        name = name,
        phoneNumber = phoneNumber,
        eMailAddress = eMailAddress
      )
    }

  implicit lazy val arbitrarySupportingDocumentType02: Arbitrary[SupportingDocumentType02] =
    Arbitrary {
      for {
        sequenceNumber          <- nonEmptyString
        typeValue               <- nonEmptyString
        referenceNumber         <- nonEmptyString
        complementOfInformation <- Gen.option(nonEmptyString)
      } yield SupportingDocumentType02(
        sequenceNumber = sequenceNumber,
        typeValue = typeValue,
        referenceNumber = referenceNumber,
        complementOfInformation = complementOfInformation
      )
    }

  implicit lazy val arbitraryTransportDocumentType02: Arbitrary[TransportDocumentType02] =
    Arbitrary {
      for {
        sequenceNumber  <- nonEmptyString
        typeValue       <- nonEmptyString
        referenceNumber <- nonEmptyString
      } yield TransportDocumentType02(
        sequenceNumber = sequenceNumber,
        typeValue = typeValue,
        referenceNumber = referenceNumber
      )
    }

  implicit lazy val arbitraryLocationType02: Arbitrary[LocationType02] =
    Arbitrary {
      for {
        qualifierOfIdentification <- Gen.oneOf(Seq("W", "U", "Z"))
        unLocode                  <- Gen.option(nonEmptyString)
        country                   <- nonEmptyString
        gnss                      <- Gen.option(arbitrary[GNSSType])
        address                   <- Gen.option(arbitrary[AddressType18])
      } yield LocationType02(
        qualifierOfIdentification = qualifierOfIdentification,
        UNLocode = unLocode,
        country = country,
        GNSS = gnss,
        Address = address
      )
    }

  implicit lazy val arbitraryGNSSType: Arbitrary[GNSSType] =
    Arbitrary {
      for {
        latitude  <- nonEmptyString
        longitude <- nonEmptyString
      } yield GNSSType(
        latitude = latitude,
        longitude = longitude
      )
    }

  implicit lazy val arbitraryXMLGregorianCalendar: Arbitrary[XMLGregorianCalendar] =
    Arbitrary {
      Gen.const(XMLCalendar("2022-07-15"))
    }

  implicit lazy val arbitraryFlag: Arbitrary[Flag] =
    Arbitrary {
      Gen.oneOf(Number0, Number1)
    }

  implicit lazy val arbitraryFunctionalErrorType04: Arbitrary[FunctionalErrorType04] =
    Arbitrary {
      for {
        errorPointer           <- nonEmptyString
        errorCode              <- Gen.oneOf(AesNctsP5FunctionalErrorCodes.values)
        errorReason            <- nonEmptyString
        originalAttributeValue <- Gen.option(nonEmptyString)
      } yield FunctionalErrorType04(
        errorPointer = errorPointer,
        errorCode = errorCode,
        errorReason = errorReason,
        originalAttributeValue = originalAttributeValue
      )
    }

  implicit lazy val arbitraryInvalidationType01: Arbitrary[InvalidationType01] =
    Arbitrary {
      for {
        requestDateAndTime  <- Gen.option(arbitrary[XMLGregorianCalendar])
        decisionDateAndTime <- Gen.option(arbitrary[XMLGregorianCalendar])
        decision            <- Gen.option(arbitrary[Flag])
        initiatedByCustoms  <- arbitrary[Flag]
        justification       <- Gen.option(nonEmptyString)
      } yield InvalidationType01(
        requestDateAndTime = requestDateAndTime,
        decisionDateAndTime = decisionDateAndTime,
        decision = decision,
        initiatedByCustoms = initiatedByCustoms,
        justification = justification
      )
    }

  implicit lazy val arbitraryTypeOfControlsType: Arbitrary[TypeOfControlsType] =
    Arbitrary {
      for {
        sequenceNumber <- nonEmptyString
        typeValue      <- nonEmptyString
        text           <- Gen.option(nonEmptyString)
      } yield TypeOfControlsType(
        sequenceNumber = sequenceNumber,
        typeValue = typeValue,
        text = text
      )
    }

  implicit lazy val arbitraryRequestedDocumentType: Arbitrary[RequestedDocumentType] =
    Arbitrary {
      for {
        sequenceNumber <- nonEmptyString
        documentType   <- nonEmptyString
        description    <- Gen.option(nonEmptyString)
      } yield RequestedDocumentType(
        sequenceNumber = sequenceNumber,
        documentType = documentType,
        description = description
      )
    }

  implicit lazy val arbitraryGuaranteeReferenceType08: Arbitrary[GuaranteeReferenceType08] =
    Arbitrary {
      for {
        sequenceNumber         <- nonEmptyString
        grn                    <- nonEmptyString
        invalidGuaranteeReason <- arbitrary[Seq[InvalidGuaranteeReasonType01]]
      } yield GuaranteeReferenceType08(
        sequenceNumber = sequenceNumber,
        GRN = grn,
        InvalidGuaranteeReason = invalidGuaranteeReason
      )
    }

  implicit lazy val arbitraryInvalidGuaranteeReasonType01: Arbitrary[InvalidGuaranteeReasonType01] =
    Arbitrary {
      for {
        sequenceNumber <- nonEmptyString
        code           <- nonEmptyString
        text           <- Gen.option(nonEmptyString)
      } yield InvalidGuaranteeReasonType01(
        sequenceNumber = sequenceNumber,
        code = code,
        text = text
      )
    }

  implicit lazy val arbitraryRecoveryNotificationType: Arbitrary[RecoveryNotificationType] =
    Arbitrary {
      for {
        recoveryNotificationDate <- Gen.option(arbitrary[XMLGregorianCalendar])
        recoveryNotificationText <- Gen.option(nonEmptyString)
        amountClaimed            <- positiveBigDecimals
        currency                 <- nonEmptyString
      } yield RecoveryNotificationType(
        recoveryNotificationDate = recoveryNotificationDate,
        recoveryNotificationText = recoveryNotificationText,
        amountClaimed = amountClaimed,
        currency = currency
      )
    }

  implicit lazy val arbitraryGuarantorType06: Arbitrary[GuarantorType06] =
    Arbitrary {
      for {
        identificationNumber <- nonEmptyString
        name                 <- Gen.option(nonEmptyString)
        address              <- Gen.option(arbitrary[AddressType16])
      } yield GuarantorType06(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryCustomsOfficeOfIncidentRegistrationType02: Arbitrary[CustomsOfficeOfIncidentRegistrationType02] =
    Arbitrary {
      for {
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfIncidentRegistrationType02(
        referenceNumber = referenceNumber
      )
    }
}
