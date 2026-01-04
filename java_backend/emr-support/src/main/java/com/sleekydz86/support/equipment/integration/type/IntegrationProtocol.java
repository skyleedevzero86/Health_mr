package com.sleekydz86.support.equipment.integration.type;

public enum IntegrationProtocol {
    HL7,        // HL7 표준
    FHIR,       // FHIR 표준
    DICOM,      // DICOM 표준 (영상)
    REST,       // REST API
    SOAP,       // SOAP API
    CUSTOM      // 커스텀 프로토콜
}
