import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { SettingsPageComponent } from './settings-page-component';
import { AccountService } from '../../../../core/services/account/account.service';
import { NotificationService } from '../../../../core/services/notification/notification.service';

describe('SettingsPageComponent', () => {
  let component: SettingsPageComponent;
  let fixture: ComponentFixture<SettingsPageComponent>;
  let accountServiceSpy: jasmine.SpyObj<AccountService>;
  let notificationServiceSpy: jasmine.SpyObj<NotificationService>;

  const account = {
    id: 'account-id',
    credentialsId: 'credentials-id',
    firstName: 'Jane',
    lastName: 'Doe',
    companyName: 'Atlas Studio',
    siretNumber: '12345678901234',
    vatNumber: 'FR12345678901',
    billingEmail: 'billing@example.com',
    billingAddressLine1: '10 rue de Paris',
    billingAddressLine2: null,
    billingPostalCode: '75000',
    billingCity: 'Paris',
    billingCountry: 'France',
    createdAt: '2026-05-31T22:17:29.895Z',
    updatedAt: null,
  };

  beforeEach(async () => {
    accountServiceSpy = jasmine.createSpyObj<AccountService>('AccountService', [
      'getMyAccount',
      'updateMyAccount',
    ]);
    notificationServiceSpy = jasmine.createSpyObj<NotificationService>('NotificationService', [
      'success',
      'error',
      'info',
      'dismiss',
      'clear',
    ]);

    await TestBed.configureTestingModule({
      imports: [SettingsPageComponent],
      providers: [
        { provide: AccountService, useValue: accountServiceSpy },
        { provide: NotificationService, useValue: notificationServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SettingsPageComponent);
    component = fixture.componentInstance;
  });

  it('should create and load the current account', () => {
    accountServiceSpy.getMyAccount.and.returnValue(of(account));

    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(accountServiceSpy.getMyAccount).toHaveBeenCalled();
    expect((component as any).account()).toEqual(account);
    expect((component as any).settingsForm.getRawValue()).toEqual({
      firstName: 'Jane',
      lastName: 'Doe',
      companyName: 'Atlas Studio',
      siretNumber: '12345678901234',
      vatNumber: 'FR12345678901',
      billingEmail: 'billing@example.com',
      billingAddressLine1: '10 rue de Paris',
      billingAddressLine2: '',
      billingPostalCode: '75000',
      billingCity: 'Paris',
      billingCountry: 'France',
    });
  });

  it('should submit the current form to the account endpoint', () => {
    accountServiceSpy.getMyAccount.and.returnValue(of(account));
    accountServiceSpy.updateMyAccount.and.returnValue(
      of({
        ...account,
        billingCity: 'Lyon',
        updatedAt: '2026-06-01T08:30:00.000Z',
      })
    );

    fixture.detectChanges();
    (component as any).settingsForm.patchValue({ billingCity: 'Lyon' });

    (component as any).onSubmit();

    expect(accountServiceSpy.updateMyAccount).toHaveBeenCalledWith(
      jasmine.objectContaining({
        firstName: 'Jane',
        lastName: 'Doe',
        billingCity: 'Lyon',
      })
    );
    expect(notificationServiceSpy.success).toHaveBeenCalledWith('Account updated.');
    expect((component as any).account()?.billingCity).toBe('Lyon');
  });

  it('should show an error notification when the account cannot be loaded', () => {
    accountServiceSpy.getMyAccount.and.returnValue(
      throwError(() => ({ error: { message: 'Not authorized' } }))
    );

    fixture.detectChanges();

    expect(notificationServiceSpy.error).toHaveBeenCalledWith(
      'Not authorized',
      'Settings unavailable'
    );
    expect((component as any).loadError()).toBe('Not authorized');
  });
});
