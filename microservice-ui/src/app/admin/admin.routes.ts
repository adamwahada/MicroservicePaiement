import { authGuard } from "../core/guards/auth.guard";
import { Routes } from '@angular/router';
import { ReferralCodeManagerComponent } from "./referral-code-manager/referral-code-manager.component";
import { AdminLayoutComponent } from "./admin-layout/admin-layout.component";
import { AdminDashboardFundsComponent } from "./admin-dashboard-funds/admin-dashboard-funds.component";
import { AdminDashboardUsersManagementComponent } from "./admin-dashboard-users-management/admin-dashboard-users-management.component";
import { AdminDashboardHistoryComponent } from "./admin-dashboard-history/admin-dashboard-history.component";

export const adminRoutes: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    data: { roles: ['ROLE_ADMIN'] },
    children: [
      { path: 'referral', component: ReferralCodeManagerComponent },
      {path: 'funds', component: AdminDashboardFundsComponent },
      {path: 'management', component: AdminDashboardUsersManagementComponent },
      {path: 'admin-dashboard-history', component: AdminDashboardHistoryComponent },
      { path: '', redirectTo: 'referral', pathMatch: 'full' },

    ]
  }
];