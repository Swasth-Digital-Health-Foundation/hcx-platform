import { createSlice } from '@reduxjs/toolkit'
import type { PayloadAction } from '@reduxjs/toolkit'

export interface AppDataInterface {
  appData: Object
}

interface TerminologyFilter {
  Code: string;
  Display: string;
  System: string;
}

const initialState: AppDataInterface = {
    appData: {
      termsAccepted:false,
      showTerms:false,
      showLinkedUsers:false,
      username:'',
      sidebar:"Profile",
      linkeduser: "",
      counter : false,
      stageRegister:"roleSelection",
      termSearch:<TerminologyFilter[]>([{"Code":"123","Display":"display text","System":"system"}]),
      showTermSearch:false,
      termSearchText:''
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