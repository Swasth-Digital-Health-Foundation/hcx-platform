import { Grid, Segment, Image, Message } from "semantic-ui-react"


export const Success = () => {

    return <>
        <Grid centered>
            <Grid.Row columns="1">
                <div className='banner' style={{ width: '50%', marginTop: '30px' }}>
                    <Grid.Column>
                        <Image src='/images/logo.png' style={{ width: '50px', marginRight: '20px' }} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding</b></p>
                    </Grid.Column>
                </div>
            </Grid.Row>
            <Grid.Row columns="1">
                <Segment raised padded style={{ width: '50%'}}>
                    <Message>
                        <Message.Header style={{ fontSize:'24px' }} >Onboarding is successful</Message.Header><br/>
                        <Message.Header style={{ textAlign:'left' }}>Please refer the below links for protocol details and integration specifications:</Message.Header><br/>
                        <Message.Header style={{ textAlign:'left' }}>Protocol details:</Message.Header>
                        <Message.List>
                            <Message.Item>Website - <a href='http://hcxprotocol.io'>http://hcxprotocol.io</a></Message.Item>
                            <Message.Item>Documentation link to the <a href='https://docs.swasth.app/hcx-specifications/'>latest (v0.7-draft) version of the specifications.</a></Message.Item>
                            <Message.Item>API definitions are available at <a href='https://raw.githubusercontent.com/Swasth-Digital-Health-Foundation/hcx-specs/v0.7/API%20Definitions/openapi_hcx.yaml'>OpenAPI 3.0 definition.</a></Message.Item>
                            <Message.Item>Data models and suggested terminologies are available in <a href='https://swasth-digital-health-foundation.github.io/standards/v0.7/index.html'>FHIR Implementation Guide.</a></Message.Item>
                            <Message.Item>The GitHub source for the latest version (v0.7-draft) of the open protocol is <a href='https://github.com/hcx-project/hcx-specs/tree/v0.7'>here.</a></Message.Item>
                        </Message.List><br/>
                        <Message.Header style={{ textAlign:'left' }}>Community details:</Message.Header>
                        <Message.List>
                            <Message.Item>GitHub community discussions on the current open protocol work and new proposals can be accessed <a href='https://github.com/hcx-project/hcx-specs/discussions'>here</a></Message.Item>
                            <Message.Item>Please join the following Discord channels where we have a robust community presence ().
                            <Message.List>
                                <Message.Item><a href='https://discord.gg/YuzBdU9chu'>Link to join the Tech & Domain Channel</a></Message.Item>
                                <Message.Item><a href='https://discord.gg/ZDcThMf4QU'>Link to join the Policy Working Channel</a></Message.Item>
                            </Message.List>
                            </Message.Item>
                        </Message.List><br/>
                        <Message.Header style={{ textAlign:'left' }}>Sandbox details:</Message.Header>
                        <Message.List>
                            <Message.Item>The integration document with links to the environment details is <a href='https://github.com/Swasth-Digital-Health-Foundation/hcx-platform/wiki/Integration-details-for-HCX-Sandbox'>here.</a></Message.Item>
                            <Message.Item>HCX Integrator SDK <a href='https://github.com/Swasth-Digital-Health-Foundation/hcx-platform/wiki/HCX-Integration-SDK-User-Manual'>details.</a></Message.Item>
                            <Message.Item>FHIR <a href='https://github.com/Swasth-Digital-Health-Foundation/hcx-platform/wiki/HCX-Integration-SDK-User-Manual'>examples.</a></Message.Item>
                            <Message.Item>FHIR examples using HAPI Fhir. Example codes are available in this <a href='https://github.com/saurabh-singh-17/hcx_sdk/tree/v0.7.1'>repo.</a></Message.Item>
                        </Message.List>
                    </Message>
                </Segment>
            </Grid.Row>
        </Grid>
    </>

}