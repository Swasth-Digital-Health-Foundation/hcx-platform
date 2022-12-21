# Data migration script

- This script is to update existing audit events with new fields. The script will take the 2 inputs, one is
  participant.json(participant details) and second is input.json(existing audit data). The script will process and gives
  an output.json(updated audit data).

## Pre-requisites :

` Nodejs -v` : v18.12.1

## Steps:

1. Export existing elastic search audit-data into JSON file using curl command.

```shell
 curl -o input.json --location --request GET '<http://localhost:9200/<index_name>/_search?pretty=true&size=10000
```

2. Need to fetch partcipants list from registry using search API and save it in participants.json file.

```json
[
  {}
  //participant details
]
```

3. Go to /data-migration folder and run the below command to execute the script and an output.json will be generated.

```shell
node app.js
```

4. Need to `Reindex` existing elastic search data to new index .

- curl command to create new index with new mapping .

```shell
curl -X PUT "localhost:9200/my-new-index-000001?pretty" -H 'Content-Type: application/json' -d'
{
  "settings": {},
  "mappings": {}
}
```

5. Alias the new index with hcx_audit index.

```shell
curl -X POST "localhost:9200/_aliases?pretty" -H 'Content-Type: application/json' -d'
{
  "actions": [
    {
      "add": {
        "index": "my-new-index-000001",
        "alias": "hcx_audit"
      }
    }
  ]
}
'
```

6. Using Bulk API will insert data into elastic search database.

```shell
 curl -H 'Content-Type: application/x-ndjson' -XPOST 'localhost:9200/index-name/_doc/_bulk?pretty' --data-binary @output.json
```


