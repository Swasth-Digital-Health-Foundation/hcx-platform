import { Route, Switch } from 'react-router-dom';

const useRoute = routesConfig => {
    const routes = routesConfig.map((route, index) => {
        const { children = [], data = {}, component: Component, ...rest } = route;
        return (
            <Route {...rest} key={index}>
                <Component routes={children} data={data} />
            </Route>
        )
    })
    return <Switch> {routes} </Switch>
}
export default useRoute;