import { Grid, Segment, Image } from "semantic-ui-react"


export const End = () => {

    return <>
        <Grid centered>
            <Grid.Row columns="1">
                <div className='banner' style={{ width: '35%', marginTop: '30px' }}>
                    <Grid.Column>
                        <Image src='/images/logo.png' style={{ width: '50px', marginRight: '20px' }} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding</b></p>
                    </Grid.Column>
                </div>
            </Grid.Row>
            <Grid.Row columns="1">
                <Segment raised padded style={{ width: '35%', height: '150%'}}>
                    <Grid.Row columns="1">
                        <div style={{ marginTop: '130px' }}>
                            <Grid.Column>
                                <p style={{ color: 'black', fontSize: '30px' }}><b>Onboarding Successful</b><br/><b>Welcome to HCX</b></p>
                            </Grid.Column>
                        </div>
                    </Grid.Row>
                </Segment>
            </Grid.Row>
        </Grid>
    </>

}