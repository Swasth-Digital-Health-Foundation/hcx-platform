package io.hcxprotocol.createresource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import io.hcxprotocol.validator.HCXFHIRValidator;
import junit.framework.Assert;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class HCXInsurancePlanTest {

    @Test public void validateInsurancePlanObject() throws Exception {
        FhirValidator validator = HCXFHIRValidator.getValidator();

        HCXInsurancePlan ip = createInsurancePlan();
        printFHIRObject(ip);

        ValidationResult result = validator.validateWithResult(ip);
        assertTrue(result.isSuccessful());

        List<SingleValidationMessage> messages = result.getMessages();
        assertTrue(messages.size() == 4);
        messages.forEach(message -> System.out.println(message.getSeverity() + " -- " + message.getLocationString() + " -- " + message.getMessage()));
    }

    private void printFHIRObject(IBaseResource ip) {
        IParser p = FhirContext.forR4().newJsonParser().setPrettyPrint(true);
        String message = p.encodeResourceToString(ip);
        System.out.println(message);
    }

    private HCXInsurancePlan createInsurancePlan() {
        //creating a new HCXInsurancePlan object and validating it against the HCX SD
        HCXInsurancePlan ip =  new HCXInsurancePlan("AB_GUJ_Plan1", Enumerations.PublicationStatus.ACTIVE, "PMJAY-Mukhyamantri Amrutam & Mukhyamantri Vatsalya");

        //Creating the Identification Extension Object
        HCXInsurancePlan.IdentificationExtension idExt = new HCXInsurancePlan.IdentificationExtension();
        idExt.setProofOfIdDocumentCode("12345","Aadhar Card");
        idExt.setClinicalDiagnosticDocumentClaimUse("preauthorization");

        //Creating Presence Extension Object
        HCXInsurancePlan.PresenceExtension preExt = new HCXInsurancePlan.PresenceExtension();
        preExt.setProofOfPresenceDocumentCode("12345","Aadhar Verification XML");
        preExt.setClinicalDiagnosticDocumentClaimUse("preauthorization");

        //Adding the above extensions to Insurance Plan Object
        HCXInsurancePlan.InsurancePlanPlanComponent plan = new HCXInsurancePlan.InsurancePlanPlanComponent();
        plan.setIdentificationExtension(idExt);
        plan.setPresenceExtension(preExt);
        plan.setType("PMJAY_GUJ_GOLD_CARD");

        //specific cost
        InsurancePlan.InsurancePlanPlanSpecificCostComponent spc = new InsurancePlan.InsurancePlanPlanSpecificCostComponent();
        spc.setCategory(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode("Inpatient-packages").setSystem("http://terminologyServer/ValueSets/cost-category"))));

        //specific cost benefit component
        HCXInsurancePlan.PlanBenefitComponent pbf = new HCXInsurancePlan.PlanBenefitComponent();
        pbf.setType(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode("HBP_PACKAGE_00003").setSystem("http://terminologyServer/ValueSets/packages"))));

        //adding extensions to benefit component
        HCXInsurancePlan.DiagnosticDocumentsExtension dde =  new HCXInsurancePlan.DiagnosticDocumentsExtension();
        dde.setClinicalDiagnosticDocumentCode(new CodeableConcept(new Coding().setSystem("https://hcx-valuesets/proofOfIdentificationDocumentCodes").setCode("MAND0001").setVersion("1.0.0").setDisplay("Post Treatment clinical photograph")));
        dde.setDocumentationUrl(new UrlType("http://documentation-url"));
        dde.setClinicalDiagnosticDocumentClaimUse(new CodeType("claim"));

        HCXInsurancePlan.InformationalMessagesExtension ime =  new HCXInsurancePlan.InformationalMessagesExtension();
        ime.setInformationalMessagesCode(new CodeableConcept(new Coding().setSystem("https://hcx-valuesets/InformationalMessagesCodes").setCode("12343").setVersion("1.0.0").setDisplay("Information Message 1")));
        ime.setDocumentationUrl(new UrlType("http://documntation-url"));
        ime.setInformationalMessageClaimUse(new CodeType("claim"));

        HCXInsurancePlan.QuestionnairesExtension qe = new HCXInsurancePlan.QuestionnairesExtension();
        qe.setQuestionnaire(new Reference("Questionnnaire/1"));
        qe.setDocumentationUrl(new UrlType("http://documentation-url"));
        qe.setQuestionnaireClaimUse(new CodeType("claim"));

        pbf.setDiagnosticDocumentsExtension(dde);
        pbf.setInformationalMessagesExtension(ime);
        pbf.setQuestionnairesExtension(qe);


        InsurancePlan.PlanBenefitCostComponent pbc =  new InsurancePlan.PlanBenefitCostComponent();
        pbc.setType(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode("hospitalization").setSystem("http://terminologyServer/ValueSets/pacakgeCostTypes"))));

        pbf.addCost(pbc);
        spc.addBenefit(pbf);

        plan.addSpecificCost(spc);
        ip.addPlan(plan);
        return ip;
    }
}
