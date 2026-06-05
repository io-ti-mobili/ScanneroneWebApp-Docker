import { Routes } from '@angular/router';
import { Dashboard } from './components/dashboard/dashboard';
import { Classifica } from './components/classifica/classifica';

export const routes: Routes = [
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
    { path: 'dashboard', component: Dashboard },
    { path: 'classifica', component: Classifica }
];
