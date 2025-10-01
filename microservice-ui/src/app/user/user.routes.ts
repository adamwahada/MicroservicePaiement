import { authGuard } from "../core/guards/auth.guard";
import { Routes } from '@angular/router';
import { UserLayoutComponent } from "./user-layout/user-layout.component";
export const userRoutes: Routes = [
  {
    path: '',
    component: UserLayoutComponent,
    canActivate: [authGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_USER'] },
    children: [
      {path: 'landingpage', loadComponent: () => import('../landingpage/landingpage.component').then(m => m.LandingpageComponent)},
 ]}
];