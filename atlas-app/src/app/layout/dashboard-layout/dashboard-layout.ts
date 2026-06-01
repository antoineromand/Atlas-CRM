import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth/auth.service';
import { AuthStateService } from '../../core/services/auth/auth-state.service';
import {BrandMarkComponent} from '../../shared/ui/brand-mark/brand-mark.component';

interface DashboardNavItem {
  label: string;
  icon: string;
  route?: string;
  disabled?: boolean;
}

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet, BrandMarkComponent],
  templateUrl: './dashboard-layout.html',
  styleUrl: './dashboard-layout.scss',
})
export class DashboardLayoutComponent {
  protected readonly menuOpen = signal(false);
  protected readonly profileMenuOpen = signal(false);
  protected readonly sidebarItems: DashboardNavItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { label: 'Clients', icon: 'group', disabled: true },
    { label: 'Invoices/Quotes', icon: 'receipt_long', disabled: true },
    { label: 'Missions', icon: 'assignment', route: '/dashboard/missions' },
  ];

  private readonly authService = inject(AuthService);
  private readonly authStateService = inject(AuthStateService);
  private readonly router = inject(Router);

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  toggleMenu(): void {
    this.menuOpen.update((current) => !current);
  }

  closeProfileMenu(): void {
    this.profileMenuOpen.set(false);
  }

  toggleProfileMenu(): void {
    this.profileMenuOpen.update((current) => !current);
  }

  openMissionCreator(): void {
    void this.router.navigate(['/dashboard/missions'], {
      queryParams: { create: '1' },
    });
    this.closeMenu();
  }

  logout(): void {
    this.closeProfileMenu();
    this.authService.logout().subscribe({
      next: () => this.finishLogout(),
      error: () => this.finishLogout(),
    });
  }

  private finishLogout(): void {
    this.authStateService.clear();
    void this.router.navigate(['/login']);
  }
}
