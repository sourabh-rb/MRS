/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.mobile.activities.blepatientlist;

import android.support.annotation.Nullable;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;
import org.openmrs.mobile.models.Patient;

import java.util.List;

public interface BlePatientListContract {

    interface View extends BaseView<Presenter> {

        void updateAdapter(List<Patient> patientList);

        void updateListVisibility(boolean isVisible, int emptyListTextStringId, @Nullable String replacementWord);

        void startEncounterForPatient(String selectedPatientID);

    }

    interface Presenter extends BasePresenterContract {

        void setQuery(String query);

        void updatePatientsList();

    }

}
