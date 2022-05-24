# Local Installation Guide


###Prerequisites

1. **[Node](https://nodejs.org/en/download/)** | > 14.x.x (Install the latest release of LTS version)

2. Configure the following Environment variables.


          | Environment Variable     | Description
          | :------------------------|----------------------------------------
          |  AUTH_TOKEN              | Bearer Token
          |  SENDER_CODE             | Uuid of Participant Code
          |  PORT                    | Server Port
          |  HCX_UPSTREAM            | HCX Gateway url

### Installation Steps

1. Client folder
a. `npm i` to install dependencies.
b. `npm run build` to build the react app
2. Open server folder
a. `npm i` to install dependencies.
b. `npm run debug` to run the server in debug mode (local development) or
c. `npm run start` to run the server in prod mode.
3. You can also use the dockerfile to build and run the app container (Optional).