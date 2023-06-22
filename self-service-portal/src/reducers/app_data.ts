import { createSlice } from '@reduxjs/toolkit'
import type { PayloadAction } from '@reduxjs/toolkit'

export interface AppDataInterface {
  appData: Object
}

const initialState: AppDataInterface = {
    appData: {
      termsAccepted:false,
      showTerms:false,
      showLinkedUsers:false,
      username:'',
      sidebar:"Profile"
    },
}

export const appDataReducer = createSlice({
  name: 'appDataStore',
  initialState,
  reducers: {
    addAppData: (state, action: PayloadAction<Object>) => {
      // Redux Toolkit allows us to write "mutating" logic in reducers. It
      // doesn't actually mutate the state because it uses the Immer library,
      // which detects changes to a "draft state" and produces a brand new
      // immutable state based off those changes
      state.appData = {...state.appData, ...action.payload}
    }
  },
})

// Action creators are generated for each case reducer function
export const { addAppData } = appDataReducer.actions

export default appDataReducer.reducer