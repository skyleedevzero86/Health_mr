package com.sleekydz86.fhir.mapper;

import com.sleekydz86.domain.user.entity.UserEntity;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class PractitionerFhirMapper {
    public Practitioner toFhir(UserEntity source) {
        Practitioner target = new Practitioner();
        target.setId(String.valueOf(source.getId()));
        target.addIdentifier().setSystem("urn:oid:kr-emr:practitioner-id").setValue(String.valueOf(source.getId()));
        if (source.getEmployeeNo() != null) {
            target.addIdentifier().setSystem("urn:oid:kr-emr:employee-number").setValue(source.getEmployeeNo());
        }
        target.addName().setText(source.getName());
        target.setActive(source.isApproved());
        if (source.getGender() != null) target.setGender(Enumerations.AdministrativeGender.fromCode(source.getGender().name().toLowerCase()));
        if (source.getTelNumValue() != null) target.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.PHONE).setValue(source.getTelNumValue()));
        if (source.getEmailValue() != null) target.addTelecom(new ContactPoint().setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue(source.getEmailValue()));
        return target;
    }
}
