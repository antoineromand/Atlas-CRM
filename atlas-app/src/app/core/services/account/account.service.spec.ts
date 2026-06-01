import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { AccountService } from './account.service';

describe('AccountService', () => {
  let service: AccountService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AccountService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should fetch the current account', () => {
    const response = {
      id: 'account-id',
      credentialsId: 'credentials-id',
      firstName: 'Jane',
      lastName: 'Doe',
      companyName: 'Atlas',
      siretNumber: null,
      vatNumber: null,
      billingEmail: 'billing@example.com',
      billingAddressLine1: '10 rue de Paris',
      billingAddressLine2: null,
      billingPostalCode: '75000',
      billingCity: 'Paris',
      billingCountry: 'France',
      createdAt: '2026-05-31T22:17:29.895Z',
      updatedAt: null,
    };

    service.getMyAccount().subscribe((account) => {
      expect(account).toEqual(response);
    });

    const req = httpMock.expectOne('http://localhost:3000/api/v1/account/me');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBeTrue();
    req.flush(response);
  });

  it('should update the current account', () => {
    const payload = {
      firstName: 'Jane',
      lastName: 'Doe',
      billingCity: 'Lyon',
    };
    const response = {
      id: 'account-id',
      credentialsId: 'credentials-id',
      firstName: 'Jane',
      lastName: 'Doe',
      companyName: 'Atlas',
      siretNumber: null,
      vatNumber: null,
      billingEmail: 'billing@example.com',
      billingAddressLine1: '10 rue de Paris',
      billingAddressLine2: null,
      billingPostalCode: '69000',
      billingCity: 'Lyon',
      billingCountry: 'France',
      createdAt: '2026-05-31T22:17:29.895Z',
      updatedAt: '2026-06-01T08:30:00.000Z',
    };

    service.updateMyAccount(payload).subscribe((account) => {
      expect(account).toEqual(response);
    });

    const req = httpMock.expectOne('http://localhost:3000/api/v1/account/me');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.withCredentials).toBeTrue();
    expect(req.request.body).toEqual(payload);
    req.flush(response);
  });
});
