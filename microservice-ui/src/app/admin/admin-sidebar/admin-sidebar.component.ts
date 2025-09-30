import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import routesData from '../../../assets/data/routes.json';
import { CommonModule } from '@angular/common';
import { KeycloakService } from '../../keycloak.service';
@Component({
  selector: 'app-admin-sidebar',
  standalone: true,
  imports: [RouterModule, CommonModule],
  template: `
    <nav class="admin-sidebar">
      <ul>
        <li *ngFor="let route of routes; let i = index">
          <div class="menu-parent" (click)="toggle(i)">
            <span *ngIf="route.icon" class="material-icons">{{route.icon}}</span>
            {{ route.title }}
            <span *ngIf="route.submenu?.length" class="arrow">
              {{ openIndex === i ? 'expand_less' : 'expand_more' }}
            </span>
          </div>
          <ul *ngIf="route.submenu?.length && openIndex === i" class="submenu">
            <li *ngFor="let sub of route.submenu">
              <a [routerLink]="sub.path" routerLinkActive="active">
                {{ sub.title }}
              </a>
            </li>
          </ul>
        </li>
      </ul>
    </nav>
  `,
  styleUrls: ['./admin-sidebar.component.scss']
})
export class AdminSidebarComponent implements OnInit {
  routes: any[] = [];
  allRoutes: any[] = (routesData as any).routes;
  openIndex: number | null = null;
  userRoles: string[] = [];

  constructor(private keycloakService: KeycloakService) {}

  ngOnInit() {
    this.userRoles = this.keycloakService.getUserRoles().map(r => r.replace('ROLE_', ''));
    this.routes = this.filterRoutesForRoles(this.allRoutes, this.userRoles);
  }

  toggle(index: number) {
    this.openIndex = this.openIndex === index ? null : index;
  }

  filterRoutesForRoles(routes: any[], userRoles: string[]): any[] {
    return routes
      .filter(route => route.role.some((role: string) => userRoles.includes(role)))
      .map(route => {
        if (route.submenu && route.submenu.length) {
          route.submenu = this.filterRoutesForRoles(route.submenu, userRoles);
        }
        return route;
      });
  }
}
