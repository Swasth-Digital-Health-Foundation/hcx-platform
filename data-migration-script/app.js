const fs = require("fs");
var _ = require("underscore");


// reading participant.json file & parsing
fs.readFile('./participants.json', function (err, data) {
    const participants = JSON.parse(data);
    const participantList = _.map(participants, function (a) {
        return {
            "participant_name": a.participant_name,
            "primary_email": a.primary_email,
            "participant_code": a.participant_code,
            "roles": a.roles
        };

    });
    const participantMap = _.groupBy(participantList, function (a) {
        return a.participant_code;
    })

    // reading input.json file & parsing
    fs.readFile("./input.json", function (err, data) {

        if (err) throw err;

        // Converting to JSON
        const esData = JSON.parse(data);
        const documents = esData.hits.hits;
        const validDocuments = _.filter(documents, function (a) {
            return (a._source["x-hcx-sender_code"] != null && a._source["x-hcx-sender_code"] != '' && a._source["x-hcx-recipient_code"] != null);
        })

        const updateDocuments = _.map(validDocuments, function (doc) {
            const payload = doc._source
            const senderCode = payload["x-hcx-sender_code"];
            const recipientCode = payload["x-hcx-recipient_code"];
            const senderDoc = _.first(participantMap[senderCode]);
            const recipientDoc = _.first(participantMap[recipientCode])
            if ((!_.isEmpty(senderDoc)) && (!_.isEmpty(recipientDoc))) {
                payload.senderName = senderDoc.participant_name;
                payload.senderPrimaryEmail = senderDoc.primary_email;
                payload.senderRole = senderDoc.roles;
                payload.recipientName = recipientDoc.participant_name;
                payload.recipientPrimaryEmail = recipientDoc.primary_email;
                payload.recipientRole = recipientDoc.roles;
            }
            return payload;
        });
        // adding index name  and mid to the doc
        const payload1 = _.flatten(_.map(updateDocuments, doc => [{
            index: { _id: doc.mid }
        }, { ...doc }]))
       // reformating 
        let myArray_without_commas = payload1.map(row => {
              return JSON.stringify(row);

        }).join("\n")
       // console.log(myArray_without_commas)
        fs.writeFileSync('./output.json', myArray_without_commas, 'utf-8'); 

    });
});