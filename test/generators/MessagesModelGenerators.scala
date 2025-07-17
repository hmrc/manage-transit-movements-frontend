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

import generated.*
import models.departureP5.BusinessRejectionType.DepartureBusinessRejectionType
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import scalaxb.XMLCalendar

import javax.xml.datatype.XMLGregorianCalendar

trait MessagesModelGenerators {
  self: Generators =>

  implicit lazy val arbitraryCC009CType: Arbitrary[CC009CType] =
    Arbitrary {
      for {
        messageSequence1            <- arbitrary[MESSAGESequence]
        transitOperation            <- arbitrary[TransitOperationType56]
        invalidation                <- arbitrary[InvalidationType02]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType05]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType23]
      } yield CC009CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        Invalidation = invalidation,
        CustomsOfficeOfDeparture = customsOfficeOfDeparture,
        HolderOfTheTransitProcedure = holderOfTheTransitProcedure,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC035CType: Arbitrary[CC035CType] =
    Arbitrary {
      for {
        messageSequence1                   <- arbitrary[MESSAGESequence]
        transitOperation                   <- arbitrary[TransitOperationType60]
        recoveryNotification               <- arbitrary[RecoveryNotificationType]
        customsOfficeOfDeparture           <- arbitrary[CustomsOfficeOfDepartureType05]
        customsOfficeOfRecoveryAtDeparture <- arbitrary[CustomsOfficeOfRecoveryAtDepartureType01]
        holderOfTheTransitProcedure        <- arbitrary[HolderOfTheTransitProcedureType13]
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
        transitOperation            <- arbitrary[TransitOperationType14]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType05]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType13]
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
        transitOperation            <- arbitrary[TransitOperationType60]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType05]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType07]
        guaranteeReferences         <- arbitrary[Seq[GuaranteeReferenceType07]]
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
        transitOperation            <- arbitrary[TransitOperationType16]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType05]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType15]
        representative              <- Gen.option(arbitrary[RepresentativeType01])
        functionalErrors            <- listWithMaxLength[FunctionalErrorType02]()
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

  implicit lazy val arbitraryCC025CType: Arbitrary[CC025CType] =
    Arbitrary {
      for {
        messageSequence1                 <- arbitrary[MESSAGESequence]
        transitOperation                 <- arbitrary[TransitOperationType07]
        customsOfficeOfDestinationActual <- arbitrary[CustomsOfficeOfDestinationActualType03]
        traderAtDestination              <- arbitrary[TraderAtDestinationType02]
      } yield CC025CType(
        messageSequence1 = messageSequence1,
        TransitOperation = transitOperation,
        CustomsOfficeOfDestinationActual = customsOfficeOfDestinationActual,
        TraderAtDestination = traderAtDestination,
        Consignment = None,
        attributes = Map.empty
      )
    }

  implicit lazy val arbitraryCC057CType: Arbitrary[CC057CType] =
    Arbitrary {
      for {
        messageSequence1                 <- arbitrary[MESSAGESequence]
        transitOperation                 <- arbitrary[TransitOperationType18]
        customsOfficeOfDestinationActual <- arbitrary[CustomsOfficeOfDestinationActualType03]
        traderAtDestination              <- arbitrary[TraderAtDestinationType02]
        functionalErrors                 <- listWithMaxLength[FunctionalErrorType07]()
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
        transitOperation            <- arbitrary[TransitOperationType20]
        customsOfficeOfDeparture    <- arbitrary[CustomsOfficeOfDepartureType05]
        holderOfTheTransitProcedure <- arbitrary[HolderOfTheTransitProcedureType23]
        representative              <- Gen.option(arbitrary[RepresentativeType06])
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
        transitOperation                    <- arbitrary[TransitOperationType57]
        customsOfficeOfDeparture            <- arbitrary[CustomsOfficeOfDepartureType05]
        customsOfficeOfIncidentRegistration <- arbitrary[CustomsOfficeOfIncidentRegistrationType02]
        consignment                         <- arbitrary[ConsignmentType28]
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
        messageSender          <- nonEmptyString
        messageRecipient       <- nonEmptyString
        preparationDateAndTime <- arbitrary[XMLGregorianCalendar]
        messageIdentification  <- nonEmptyString
        messageType            <- arbitrary[MessageTypes]
        correlationIdentifier  <- Gen.option(nonEmptyString)
      } yield MESSAGESequence(
        messageSender = messageSender,
        messageRecipient = messageRecipient,
        preparationDateAndTime = preparationDateAndTime,
        messageIdentification = messageIdentification,
        messageType = messageType,
        correlationIdentifier = correlationIdentifier
      )
    }

  implicit lazy val arbitraryMessageTypes: Arbitrary[MessageTypes] =
    Arbitrary {
      Gen.oneOf(MessageTypes.values)
    }

  implicit lazy val arbitraryConsignmentType28: Arbitrary[ConsignmentType28] =
    Arbitrary {
      for {
        incident <- listWithMaxLength[IncidentType02]()
      } yield ConsignmentType28(
        Incident = incident
      )
    }

  implicit lazy val arbitraryTransitOperationType56: Arbitrary[TransitOperationType56] =
    Arbitrary {
      for {
        lrn <- Gen.option(nonEmptyString)
        mrn <- Gen.option(nonEmptyString)
      } yield TransitOperationType56(
        LRN = lrn,
        MRN = mrn
      )
    }

  implicit lazy val arbitraryTransitOperationType07: Arbitrary[TransitOperationType07] =
    Arbitrary {
      for {
        mrn              <- nonEmptyString
        releaseDate      <- arbitrary[XMLGregorianCalendar]
        releaseIndicator <- nonEmptyString
      } yield TransitOperationType07(
        MRN = mrn,
        releaseDate = releaseDate,
        releaseIndicator = releaseIndicator
      )
    }

  implicit lazy val arbitraryTransitOperationType14: Arbitrary[TransitOperationType14] =
    Arbitrary {
      for {
        mrn                              <- nonEmptyString
        declarationSubmissionDateAndTime <- arbitrary[XMLGregorianCalendar]
        noReleaseMotivationCode          <- nonEmptyString
        noReleaseMotivationText          <- nonEmptyString
      } yield TransitOperationType14(
        MRN = mrn,
        declarationSubmissionDateAndTime = declarationSubmissionDateAndTime,
        noReleaseMotivationCode = noReleaseMotivationCode,
        noReleaseMotivationText = noReleaseMotivationText
      )
    }

  implicit lazy val arbitraryTransitOperationType16: Arbitrary[TransitOperationType16] =
    Arbitrary {
      for {
        lrn                   <- Gen.option(nonEmptyString)
        mrn                   <- Gen.option(nonEmptyString)
        businessRejectionType <- arbitrary[DepartureBusinessRejectionType].map(_.value)
        rejectionDateAndTime  <- arbitrary[XMLGregorianCalendar]
        rejectionCode         <- nonEmptyString
        rejectionReason       <- Gen.option(nonEmptyString)
      } yield TransitOperationType16(
        LRN = lrn,
        MRN = mrn,
        businessRejectionType = businessRejectionType,
        rejectionDateAndTime = rejectionDateAndTime,
        rejectionCode = rejectionCode,
        rejectionReason = rejectionReason
      )
    }

  implicit lazy val arbitraryTransitOperationType18: Arbitrary[TransitOperationType18] =
    Arbitrary {
      for {
        mrn                   <- nonEmptyString
        businessRejectionType <- arbitrary[DepartureBusinessRejectionType].map(_.value)
        rejectionDateAndTime  <- arbitrary[XMLGregorianCalendar]
        rejectionCode         <- nonEmptyString
        rejectionReason       <- Gen.option(nonEmptyString)
      } yield TransitOperationType18(
        MRN = mrn,
        businessRejectionType = businessRejectionType,
        rejectionDateAndTime = rejectionDateAndTime,
        rejectionCode = rejectionCode,
        rejectionReason = rejectionReason
      )
    }

  implicit lazy val arbitraryTransitOperationType20: Arbitrary[TransitOperationType20] =
    Arbitrary {
      for {
        lrn                            <- Gen.option(nonEmptyString)
        mrn                            <- Gen.option(nonEmptyString)
        controlNotificationDateAndTime <- arbitrary[XMLGregorianCalendar]
        notificationType               <- nonEmptyString
      } yield TransitOperationType20(
        LRN = lrn,
        MRN = mrn,
        controlNotificationDateAndTime = controlNotificationDateAndTime,
        notificationType = notificationType
      )
    }

  implicit lazy val arbitraryTransitOperationType57: Arbitrary[TransitOperationType57] =
    Arbitrary {
      for {
        mrn                             <- nonEmptyString
        incidentNotificationDateAndTime <- arbitrary[XMLGregorianCalendar]
      } yield TransitOperationType57(
        MRN = mrn,
        incidentNotificationDateAndTime = incidentNotificationDateAndTime
      )
    }

  implicit lazy val arbitraryTransitOperationType60: Arbitrary[TransitOperationType60] =
    Arbitrary {
      for {
        mrn                       <- nonEmptyString
        declarationAcceptanceDate <- arbitrary[XMLGregorianCalendar]
      } yield TransitOperationType60(
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

  implicit lazy val arbitraryCustomsOfficeOfDepartureType05: Arbitrary[CustomsOfficeOfDepartureType05] =
    Arbitrary {
      for {
        referenceNumber <- nonEmptyString
      } yield CustomsOfficeOfDepartureType05(
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

  implicit lazy val arbitraryHolderOfTheTransitProcedureType07: Arbitrary[HolderOfTheTransitProcedureType07] =
    Arbitrary {
      for {
        identificationNumber <- Gen.option(nonEmptyString)
        name                 <- Gen.option(nonEmptyString)
        address              <- Gen.option(arbitrary[AddressType02])
      } yield HolderOfTheTransitProcedureType07(
        identificationNumber = identificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType15: Arbitrary[HolderOfTheTransitProcedureType15] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
        address                       <- Gen.option(arbitrary[AddressType14])
      } yield HolderOfTheTransitProcedureType15(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = address
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType23: Arbitrary[HolderOfTheTransitProcedureType23] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
        address                       <- Gen.option(arbitrary[AddressType14])
        contactPerson                 <- Gen.option(arbitrary[ContactPersonType03])
      } yield HolderOfTheTransitProcedureType23(
        identificationNumber = identificationNumber,
        TIRHolderIdentificationNumber = tirHolderIdentificationNumber,
        name = name,
        Address = address,
        ContactPerson = contactPerson
      )
    }

  implicit lazy val arbitraryHolderOfTheTransitProcedureType13: Arbitrary[HolderOfTheTransitProcedureType13] =
    Arbitrary {
      for {
        identificationNumber          <- Gen.option(nonEmptyString)
        tirHolderIdentificationNumber <- Gen.option(nonEmptyString)
        name                          <- Gen.option(nonEmptyString)
        address                       <- Gen.option(arbitrary[AddressType14])
      } yield HolderOfTheTransitProcedureType13(
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
        contactPerson        <- Gen.option(arbitrary[ContactPersonType03])
      } yield RepresentativeType03(
        identificationNumber = identificationNumber,
        ContactPerson = contactPerson
      )
    }

  implicit lazy val arbitraryRepresentativeType06: Arbitrary[RepresentativeType06] =
    Arbitrary {
      for {
        identificationNumber <- nonEmptyString
        status               <- nonEmptyString
        contactPerson        <- Gen.option(arbitrary[ContactPersonType03])
      } yield RepresentativeType06(
        identificationNumber = identificationNumber,
        status = status,
        ContactPerson = contactPerson
      )
    }

  implicit lazy val arbitraryTraderAtDestinationType02: Arbitrary[TraderAtDestinationType02] =
    Arbitrary {
      for {
        identificationNumber <- nonEmptyString
      } yield TraderAtDestinationType02(
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

  implicit lazy val arbitraryTransportEquipmentType06: Arbitrary[TransportEquipmentType06] =
    Arbitrary {
      for {
        sequenceNumber                <- arbitrary[BigInt]
        containerIdentificationNumber <- Gen.option(nonEmptyString)
        seals                         <- arbitrary[Seq[SealType01]]
        numberOfSeals                 <- Gen.option(positiveBigInts)
        goodsReferences               <- arbitrary[Seq[GoodsReferenceType03]]
      } yield TransportEquipmentType06(
        sequenceNumber = sequenceNumber,
        containerIdentificationNumber = containerIdentificationNumber,
        numberOfSeals = numberOfSeals,
        Seal = seals,
        GoodsReference = goodsReferences
      )
    }

  implicit lazy val arbitrarySealType01: Arbitrary[SealType01] =
    Arbitrary {
      for {
        sequenceNumber <- arbitrary[BigInt]
        identifier     <- nonEmptyString
      } yield SealType01(
        sequenceNumber = sequenceNumber,
        identifier = identifier
      )
    }

  implicit lazy val arbitraryGoodsReferenceType01: Arbitrary[GoodsReferenceType01] =
    Arbitrary {
      for {
        sequenceNumber             <- arbitrary[BigInt]
        declarationGoodsItemNumber <- positiveBigInts
      } yield GoodsReferenceType01(
        sequenceNumber = sequenceNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber
      )
    }

  implicit lazy val arbitraryGoodsReferenceType03: Arbitrary[GoodsReferenceType03] =
    Arbitrary {
      for {
        sequenceNumber             <- arbitrary[BigInt]
        declarationGoodsItemNumber <- positiveBigInts
      } yield GoodsReferenceType03(
        sequenceNumber = sequenceNumber,
        declarationGoodsItemNumber = declarationGoodsItemNumber
      )
    }

  implicit lazy val arbitraryEndorsementType02: Arbitrary[EndorsementType02] = Arbitrary {
    for {
      date      <- arbitrary[XMLGregorianCalendar]
      authority <- nonEmptyString
      place     <- nonEmptyString
      country   <- nonEmptyString
    } yield EndorsementType02(date, authority, place, country)
  }

  implicit lazy val arbitraryTranshipmentType: Arbitrary[TranshipmentType] = Arbitrary {
    for {
      containerIndicator <- arbitrary[Flag]
      transportMeans     <- arbitrary[TransportMeansType]
    } yield TranshipmentType(
      containerIndicator = containerIndicator,
      TransportMeans = transportMeans
    )
  }

  implicit lazy val arbitraryTransportMeansType: Arbitrary[TransportMeansType] = Arbitrary {
    for {
      sequenceNumber  <- nonEmptyString
      typeValue       <- nonEmptyString
      referenceNumber <- nonEmptyString
    } yield TransportMeansType(
      typeOfIdentification = sequenceNumber,
      identificationNumber = typeValue,
      nationality = referenceNumber
    )
  }

  implicit lazy val arbitraryIncidentType02: Arbitrary[IncidentType02] =
    Arbitrary {
      for {
        sequenceNumber     <- arbitrary[BigInt]
        code               <- nonEmptyString
        text               <- nonEmptyString
        loc                <- arbitrary[LocationType]
        endorsement        <- arbitrary[EndorsementType02]
        transportEquipment <- arbitrary[Seq[TransportEquipmentType06]]
        containerIndicator <- arbitrary[Flag]
        transhipment       <- arbitrary[TranshipmentType]
      } yield IncidentType02(
        sequenceNumber = sequenceNumber,
        code = code,
        text = text,
        Endorsement = Some(endorsement),
        Location = loc,
        TransportEquipment = transportEquipment,
        Transhipment = Some(transhipment)
      )
    }

  implicit lazy val arbitraryAddressType02: Arbitrary[AddressType02] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType02(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType14: Arbitrary[AddressType14] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType14(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType20: Arbitrary[AddressType20] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
        country         <- nonEmptyString
      } yield AddressType20(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city,
        country = country
      )
    }

  implicit lazy val arbitraryAddressType21: Arbitrary[AddressType21] =
    Arbitrary {
      for {
        streetAndNumber <- nonEmptyString
        postcode        <- Gen.option(nonEmptyString)
        city            <- nonEmptyString
      } yield AddressType21(
        streetAndNumber = streetAndNumber,
        postcode = postcode,
        city = city
      )
    }

  implicit lazy val arbitraryContactPersonType03: Arbitrary[ContactPersonType03] =
    Arbitrary {
      for {
        name         <- nonEmptyString
        phoneNumber  <- nonEmptyString
        eMailAddress <- Gen.option(nonEmptyString)
      } yield ContactPersonType03(
        name = name,
        phoneNumber = phoneNumber,
        eMailAddress = eMailAddress
      )
    }

  implicit lazy val arbitraryLocationType: Arbitrary[LocationType] =
    Arbitrary {
      for {
        qualifierOfIdentification <- Gen.oneOf(Seq("W", "U", "Z"))
        unLocode                  <- Gen.option(nonEmptyString)
        country                   <- nonEmptyString
        gnss                      <- Gen.option(arbitrary[GNSSType])
        address                   <- Gen.option(arbitrary[AddressType21])
      } yield LocationType(
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

  implicit lazy val arbitraryFunctionalErrorType02: Arbitrary[FunctionalErrorType02] =
    Arbitrary {
      for {
        errorPointer           <- nonEmptyString
        errorCode              <- nonEmptyString
        errorReason            <- nonEmptyString
        originalAttributeValue <- Gen.option(nonEmptyString)
      } yield FunctionalErrorType02(
        errorPointer = errorPointer,
        errorCode = errorCode,
        errorReason = errorReason,
        originalAttributeValue = originalAttributeValue
      )
    }

  implicit lazy val arbitraryFunctionalErrorType07: Arbitrary[FunctionalErrorType07] =
    Arbitrary {
      for {
        errorPointer           <- nonEmptyString
        errorCode              <- Gen.oneOf(AesNctsP5FunctionalErrorCodes.values)
        errorReason            <- nonEmptyString
        originalAttributeValue <- Gen.option(nonEmptyString)
      } yield FunctionalErrorType07(
        errorPointer = errorPointer,
        errorCode = errorCode,
        errorReason = errorReason,
        originalAttributeValue = originalAttributeValue
      )
    }

  implicit lazy val arbitraryInvalidationType02: Arbitrary[InvalidationType02] =
    Arbitrary {
      for {
        requestDateAndTime  <- Gen.option(arbitrary[XMLGregorianCalendar])
        decisionDateAndTime <- Gen.option(arbitrary[XMLGregorianCalendar])
        decision            <- Gen.option(arbitrary[Flag])
        initiatedByCustoms  <- arbitrary[Flag]
        justification       <- Gen.option(nonEmptyString)
      } yield InvalidationType02(
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
        sequenceNumber <- arbitrary[BigInt]
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
        sequenceNumber <- arbitrary[BigInt]
        documentType   <- nonEmptyString
        description    <- Gen.option(nonEmptyString)
      } yield RequestedDocumentType(
        sequenceNumber = sequenceNumber,
        documentType = documentType,
        description = description
      )
    }

  implicit lazy val arbitraryGuaranteeReferenceType07: Arbitrary[GuaranteeReferenceType07] =
    Arbitrary {
      for {
        sequenceNumber         <- arbitrary[BigInt]
        grn                    <- nonEmptyString
        invalidGuaranteeReason <- arbitrary[Seq[InvalidGuaranteeReasonType01]]
      } yield GuaranteeReferenceType07(
        sequenceNumber = sequenceNumber,
        GRN = grn,
        InvalidGuaranteeReason = invalidGuaranteeReason
      )
    }

  implicit lazy val arbitraryInvalidGuaranteeReasonType01: Arbitrary[InvalidGuaranteeReasonType01] =
    Arbitrary {
      for {
        sequenceNumber <- arbitrary[BigInt]
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
        identificationNumber <- Gen.option(nonEmptyString)
        name                 <- Gen.option(nonEmptyString)
        address              <- Gen.option(arbitrary[AddressType20])
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
